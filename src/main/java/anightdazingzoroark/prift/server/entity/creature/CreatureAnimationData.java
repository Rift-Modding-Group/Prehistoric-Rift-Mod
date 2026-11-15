package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.api.util.CreatureLookUtil;

import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.server.effect.RiftParalysisEffect;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.NotNull;

//anim data specifically for rift creatures
public class CreatureAnimationData extends AnimationDataEntityLiving {
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

    /**
     * Reads live rider pitch for moves that allow looking throughout use.
     * */
    @Override
    public float getHeadPitch(float partialTicks) {
        if (this.getHolder() instanceof RiftCreature creature) {
            CreatureMoveBuilder move = CreatureLookUtil.getRiddenAimMove(creature, creature.getCurrentMove());
            CreatureMoveChargeupBuilder charge = move == null ? null : move.getMoveChargeupBuilder();
            if (charge != null && charge.getCanLookWhileUsing() && creature.getControllingPassenger() instanceof EntityPlayer rider) {
                float[] bounds = move.getRiddenPitchBounds();
                float slopeLookResult = (float) MathUtil.slopeResult(
                        partialTicks, true, 0D, 1D,
                        rider.prevRotationPitch, rider.rotationPitch
                );
                return Math.clamp(slopeLookResult, bounds[0], bounds[1]);
            }
        }
        return super.getHeadPitch(partialTicks);
    }
}
