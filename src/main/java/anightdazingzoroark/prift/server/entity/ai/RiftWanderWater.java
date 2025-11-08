package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class RiftWanderWater extends EntityAIWander {
    private final RiftWaterCreature waterCreature;

    public RiftWanderWater(RiftWaterCreature creature) {
        super(creature, 1D, 1);
        this.waterCreature = creature;
    }

    @Override
    public boolean shouldExecute() {
        if (this.waterCreature.isSleeping()) return false;
        else {
            if (this.waterCreature.isTamed()) {
                return super.shouldExecute()
                        && this.waterCreature.creatureBoxWithinReach()
                        && !this.waterCreature.isSitting()
                        && this.waterCreature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE
                        && !this.waterCreature.busyAtWork()
                        && !this.waterCreature.isBeingRidden()
                        && this.waterCreature.isInWater();
            }
            else {
                boolean isHerdLeader = this.waterCreature.isHerdLeader();
                boolean isStrayFromHerd = !this.waterCreature.canDoHerding() || !this.waterCreature.isHerdLeader() && !this.waterCreature.hasHerdLeader();

                return super.shouldExecute() && ((isHerdLeader && this.waterCreature.isInWater()) || (isStrayFromHerd && this.waterCreature.isInWater()));
            }
        }
    }

    public boolean shouldContinueExecuting() {
        return this.waterCreature.isInWater() && super.shouldContinueExecuting();
    }

    @Nullable
    @Override
    protected Vec3d getPosition() {
        for (int i = 0; i < 10; i++) {
            int possibleXOffset = RiftUtil.randomInRange(-8, 8);
            int possibleYOffset = RiftUtil.randomInRange(-3, 3);
            int possibleZOffset = RiftUtil.randomInRange(-8, 8);

            BlockPos posToCheck = this.waterCreature.getPosition().add(possibleXOffset, possibleYOffset, possibleZOffset);
            if (!this.waterCreature.isTamed() && this.isWaterDestination(posToCheck)) return new Vec3d(posToCheck);
            else if (this.waterCreature.isTamed() && this.isWaterDestination(posToCheck) && this.withinHomeDistance(posToCheck)) return new Vec3d(posToCheck);
        }

        return null;
    }

    private boolean isWaterDestination(BlockPos pos) {
        if (pos == null) return false;

        //swimming is based on whether or not the body hitbox is in water so the check on if a
        //position is water is based on its position and height
        //if its not available for some reason, use the creatures default height
        int heightToCheck = (int) Math.ceil(this.waterCreature.height);
        if (this.waterCreature.getBodyHitbox() != null) heightToCheck = (int) Math.ceil(this.waterCreature.getBodyHitbox().height);

        for (int y = 0; y < heightToCheck; y++) {
            BlockPos posToCheck = pos.add(0, y, 0);
            if (this.waterCreature.world.getBlockState(posToCheck).getMaterial() != Material.WATER) return false;
        }
        return true;
    }

    private boolean withinHomeDistance(BlockPos pos) {
        if (pos == null || this.waterCreature.getHomePos() == null) return false;

        RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) this.waterCreature.world.getTileEntity(this.waterCreature.getHomePos());

        if (creatureBox == null) return false;

        for (int y = 0; y < Math.ceil(this.waterCreature.height); y++) {
            BlockPos posToCheck = pos.add(0, y, 0);
            if (creatureBox.posWithinDeploymentRange(posToCheck)) return false;
        }
        return true;
    }
}
