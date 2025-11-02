package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

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
            if (this.creature.isTamed()) return this.waterBlockPos != null
                    && !this.creature.isSitting()
                    && this.creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE
                    && !this.creature.isBeingRidden();
            else return this.waterBlockPos != null;
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
        this.creature.getNavigator().clearPath();
    }

    @Override
    public void updateTask() {
        this.creature.getMoveHelper().setMoveTo(this.waterBlockPos.getX(), this.waterBlockPos.getY(), this.waterBlockPos.getZ(), this.speed);
    }

    private BlockPos nearestWaterBlock() {
        //get all valid positions
        List<BlockPos> blockPosList = new ArrayList<>();
        int minXBound = -this.detectRange / 2 + (int) Math.floor(this.creature.getEntityBoundingBox().minX);
        int maxXBound = this.detectRange / 2 + (int) Math.ceil(this.creature.getEntityBoundingBox().maxX);
        int minYBound = -this.detectRange / 2 + (int) this.creature.posY;
        int maxYBound = this.detectRange / 2 + (int) this.creature.posY + (int) this.creature.height;
        int minZBound = -this.detectRange / 2 + (int) Math.floor(this.creature.getEntityBoundingBox().minZ);
        int maxZBound = this.detectRange / 2 + (int) Math.ceil(this.creature.getEntityBoundingBox().maxZ);

        for (int x = minXBound; x <= maxXBound; x++) {
            for (int y = minYBound; y <= maxYBound; y++) {
                for (int z = minZBound; z <= maxZBound; z++) {
                    BlockPos tempPos = new BlockPos(x, y, z);
                    if (this.creature.world.getBlockState(tempPos).getMaterial() == Material.WATER) {
                        if (this.canFitHitbox(tempPos)) blockPosList.add(tempPos);
                    }
                }
            }
        }

        //calculate the closest one
        if (!blockPosList.isEmpty()) {
            BlockPos closest = null;
            for (BlockPos testPos : blockPosList) {
                if (closest == null) closest = testPos;
                else {
                    if (testPos.distanceSq(this.creature.posX, this.creature.posY, this.creature.posZ) <= closest.distanceSq(this.creature.posX, this.creature.posY, this.creature.posZ)) {
                        closest = testPos;
                    }
                }
            }
            return closest;
        }
        return null;
    }

    private boolean canFitHitbox(BlockPos pos) {
        float width = Math.round(this.creature.width);
        float height = (float) Math.floor(this.creature.height);
        for (int x = -Math.round(width/2f); x <= Math.round(width/2f); x++) {
            for (int y = 0; y <= height; y++) {
                for (int z = -Math.round(width/2f); z <= Math.round(width/2f); z++) {
                    if (this.creature.world.getBlockState(pos.add(x, y, z)).getMaterial() != Material.WATER) return false;
                }
            }
        }
        return true;
    }
}
