package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class RiftWanderWater extends EntityAIWander {
    private RiftWaterCreature waterCreature;

    public RiftWanderWater(RiftWaterCreature creatureIn, double speedIn) {
        super(creatureIn, speedIn, 1);
        this.waterCreature = creatureIn;
    }

    @Override
    public boolean shouldExecute() {
        if (this.waterCreature.isTamed()) {
            if (this.waterCreature.getTameStatus() == TameStatusType.WANDER && !this.waterCreature.isBeingRidden() && this.waterCreature.isInWater()) return super.shouldExecute();
            else return false;
        }
        else {
            if (this.waterCreature.isHerdLeader() && this.waterCreature.isInWater()) return super.shouldExecute();
            else if (!this.waterCreature.isHerdLeader() && !this.waterCreature.hasHerdLeader() && this.waterCreature.isInWater()) return super.shouldExecute();
            else return false;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.waterCreature.getEnergy() > 0 && !this.waterCreature.hasHerdLeader() && this.waterCreature.isInWater() && super.shouldContinueExecuting();
    }

    @Override
    protected Vec3d getPosition() {
        Vec3d pos = RandomPositionGenerator.findRandomTarget(this.entity, 10, 7);
        if (pos != null) {
            BlockPos blockPos = new BlockPos(pos);
            for (int i = 0; !this.isWaterDestination(blockPos) && i < 10; i++) pos = RandomPositionGenerator.findRandomTarget(this.entity, 10, 7);
        }
        return pos;
    }

    @Override
    public void resetTask() {
        this.entity.getNavigator().clearPath();
    }

    private boolean isWaterDestination(BlockPos pos) {
        return this.entity.world.getBlockState(pos).getMaterial() == Material.WATER;
    }
}