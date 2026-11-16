package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.api.util.CreatureLookUtil;

import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.server.effect.RiftParalysisEffect;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

//anim data specifically for rift creatures
public class CreatureAnimationData extends AnimationDataEntityLiving {
    private long lastSyncedRayMoveUseId = -1L;
    private float lastSyncedRayPitch;

    public CreatureAnimationData(@NotNull RiftCreature creature) {
        super(creature);
    }

    @Override
    public void updateOnDataTick() {
        super.updateOnDataTick();
        if (this.getWorld() == null || this.getWorld().isRemote) return;
        double speed = RiftParalysisEffect.getSpeedMultiplier(this.getHolder());
        for (AnimationController<?, ?> controller : this.getAnimationControllers().values()) {
            if (controller.getName().startsWith("moveUse")) controller.setAnimationSpeed(speed);
        }
    }

    @Override
    @NotNull
    public NBTTagCompound getNBT() {
        NBTTagCompound data = super.getNBT();
        if (this.getHolder() instanceof RiftCreature creature && creature.getControllingPassenger() == null) {
            CreatureMoveBuilder move = CreatureLookUtil.getRiddenAimMove(creature, creature.getCurrentMove());
            CreatureMoveChargeupBuilder charge = move == null ? null : move.getMoveChargeupBuilder();
            if (charge != null && charge.getCanLookWhileUsing() && move.getRiddenAimLocator() != null) {
                float[] bounds = move.getRiddenPitchBounds();
                float pitch = (float) Math.clamp(this.getVariable("ray_release_pitch"), bounds[0], bounds[1]);
                long moveUseId = creature.getCreatureMoves().getCurrentMoveUseId();
                float previousPitch = this.lastSyncedRayMoveUseId == moveUseId ? this.lastSyncedRayPitch : pitch;
                // RiftLibrary smooths this channel on the render clock, including updates arriving between ticks.
                NBTTagCompound headRotation = data.getCompoundTag("HeadRotation");
                headRotation.setFloat("Pitch", pitch);
                headRotation.setFloat("PreviousPitch", previousPitch);
                this.lastSyncedRayMoveUseId = moveUseId;
                this.lastSyncedRayPitch = pitch;
            }
        }
        return data;
    }

    /**
     * Uses the move's bounded aim pitch for both AI and ridden ray animations.
     * */
    @Override
    public float getHeadPitch(float partialTicks) {
        if (this.getHolder() instanceof RiftCreature creature) {
            CreatureMoveBuilder move = CreatureLookUtil.getRiddenAimMove(creature, creature.getCurrentMove());
            CreatureMoveChargeupBuilder charge = move == null ? null : move.getMoveChargeupBuilder();
            if (charge != null && charge.getCanLookWhileUsing()) {
                float[] bounds = move.getRiddenPitchBounds();
                if (creature.getControllingPassenger() instanceof EntityPlayer rider) {
                    float slopeLookResult = (float) MathUtil.slopeResult(
                            partialTicks, true, 0D, 1D,
                            rider.prevRotationPitch, rider.rotationPitch
                    );
                    return Math.clamp(slopeLookResult, bounds[0], bounds[1]);
                }
                if (move.getRiddenAimLocator() != null) {
                    if (this.getWorld() != null && this.getWorld().isRemote && this.isServerSynced()) {
                        return Math.clamp(super.getHeadPitch(partialTicks), bounds[0], bounds[1]);
                    }
                    return (float) Math.clamp(this.getVariable("ray_release_pitch"), bounds[0], bounds[1]);
                }
            }
        }
        return super.getHeadPitch(partialTicks);
    }
}
