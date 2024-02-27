package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;

public class RiftGoToWater extends EntityAIBase {
    private final RiftCreature creature;
    private final int detectRange;
    private final double speed;
    protected BlockPos waterBlockPos;

    public RiftGoToWater(RiftCreature creature, int detectRange, double speed) {
        this.creature = creature;
        this.detectRange = detectRange;
        this.speed = speed;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (!this.creature.isInWater()) {
            this.waterBlockPos = this.nearestWaterBlock();
            if (!this.creature.isTamed()) return this.waterBlockPos != null;
            else return this.waterBlockPos != null && this.creature.getTameStatus() == TameStatusType.WANDER && !this.creature.isBeingRidden();
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.creature.isInWater() && this.creature.getEnergy() > 0;
    }

    @Override
    public void resetTask() {
        this.waterBlockPos = null;
    }

    @Override
    public void updateTask() {
        this.creature.getMoveHelper().setMoveTo(this.waterBlockPos.getX(), this.waterBlockPos.getY(), this.waterBlockPos.getZ(), this.speed);
    }

    private BlockPos nearestWaterBlock() {
        for (int x = -this.detectRange/2; x <= this.detectRange/2; x++) {
            for (int y = -this.detectRange/2; y <= this.detectRange/2; y++) {
                for (int z = -this.detectRange/2; z <= this.detectRange/2; z++) {
                    BlockPos tempPos = this.creature.getPosition().add(x, y, z);
                    if (this.creature.world.getBlockState(tempPos).getMaterial() == Material.WATER) {
                        if (canFitHitbox(tempPos)) return tempPos;
                    }
                }
            }
        }
        return null;
    }

    private boolean canFitHitbox(BlockPos pos) {
        float width = Math.round(this.creature.width);
        for (int x = -Math.round(width/2f); x <= Math.round(width/2f); x++) {
            for (int z = -Math.round(width/2f); z <= Math.round(width/2f); z++) {
                if (this.creature.world.getBlockState(pos.add(x, 0, z)).getMaterial() != Material.WATER) return false;
            }
        }
        return true;
    }
}
