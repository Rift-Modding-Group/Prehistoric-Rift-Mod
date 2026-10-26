package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.api.creature.RiftCreatureEnums.BaseDeploymentBehavior;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums.CreatureDeployment;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RiftFollowBoxOwner extends EntityAIBase {
    @NotNull
    private final RiftCreature creature;
    @Nullable
    private EntityLivingBase owner;
    private int recalculatePath;

    public RiftFollowBoxOwner(@NotNull RiftCreature creature) {
        this.creature = creature;
        this.setMutexBits(3);
    }

    private boolean canFollow() {
        this.owner = this.creature.getOwner();
        AxisAlignedBB bounds = this.creature.getDeploymentBounds();
        return this.creature.getDeploymentType() == CreatureDeployment.BASE
                && this.creature.getBaseDeploymentBehavior() == BaseDeploymentBehavior.FOLLOW
                && !this.creature.getIsSleeping() && !this.creature.isStaggered() && !this.creature.isBeingRidden()
                && this.creature.getAttackTarget() == null && this.owner != null && this.owner.isEntityAlive()
                && this.owner.world == this.creature.world && bounds != null
                && (!(this.owner instanceof EntityPlayer player) || !player.isSpectator());
    }

    @Override
    public boolean shouldExecute() {
        return this.canFollow() && this.creature.getDistanceSq(this.owner) >= 100D;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.canFollow() && this.creature.getDistanceSq(this.owner) > 16D;
    }

    @Override
    public void startExecuting() {
        this.recalculatePath = 0;
    }

    @Override
    public void resetTask() {
        this.owner = null;
        this.creature.getNavigator().clearPath();
    }

    @Override
    public void updateTask() {
        if (this.owner == null) return;
        this.creature.getLookHelper().setLookPositionWithEntity(this.owner, 10f, this.creature.getVerticalFaceSpeed());
        if (--this.recalculatePath <= 0) {
            this.recalculatePath = 10;
            AxisAlignedBB bounds = this.creature.getDeploymentBounds();
            if (bounds != null && !this.creature.isWithinDeploymentRange(this.owner.posX, this.owner.posY, this.owner.posZ)) {
                double margin = this.creature.width * 0.5D + 1.5D;
                BlockPos destination = new BlockPos(
                        Math.clamp(this.owner.posX, Math.ceil(bounds.minX + margin), Math.floor(bounds.maxX - margin - 0.5D)),
                        Math.clamp(this.owner.posY, Math.ceil(bounds.minY), Math.floor(bounds.maxY - this.creature.height)),
                        Math.clamp(this.owner.posZ, Math.ceil(bounds.minZ + margin), Math.floor(bounds.maxZ - margin - 0.5D))
                );
                this.creature.getCreaturePathNavigate().tryMoveToPositionUsingWater(destination, 1D);
            }
            else this.creature.getNavigator().tryMoveToEntityLiving(this.owner, 1D);
        }
    }
}
