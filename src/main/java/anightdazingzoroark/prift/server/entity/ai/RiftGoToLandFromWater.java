package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class RiftGoToLandFromWater extends EntityAIBase {
    private final RiftCreature creature;
    private final int detectRange;
    private final double speed;
    protected BlockPos landBlockPos;

    public RiftGoToLandFromWater(RiftCreature creature, int detectRange, double speed) {
        this.creature = creature;
        this.detectRange = detectRange;
        this.speed = speed;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature.isInWater()) {
            this.landBlockPos = this.nearestLandBlock();
            if (this.creature.isTamed()) return this.landBlockPos != null
                    && !this.creature.isSitting()
                    && this.creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE
                    && !this.creature.isBeingRidden();
            else return this.landBlockPos != null;
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.creature.isInWater() && this.creature.getEnergy() > 0;
    }

    @Override
    public void resetTask() {
        this.landBlockPos = null;
    }

    @Override
    public void updateTask() {
        this.creature.getLookHelper().setLookPosition(this.landBlockPos.getX(), this.landBlockPos.getY(), this.landBlockPos.getZ(), 30, 30);
        this.creature.getMoveHelper().setMoveTo(this.landBlockPos.getX(), this.landBlockPos.getY(), this.landBlockPos.getZ(), this.speed);
    }

    //look for a land block with sufficient space on top to go to
    private BlockPos nearestLandBlock() {
        //get all valid positions
        List<BlockPos> blockPosList = new ArrayList<>();
        for (int x = -this.detectRange/2; x <= this.detectRange/2; x++) {
            for (int y = -this.detectRange/2; y <= this.detectRange/2; y++) {
                for (int z = -this.detectRange/2; z <= this.detectRange/2; z++) {
                    BlockPos tempPos = this.creature.getPosition().add(x, y, z);
                    if (this.creature.world.getBlockState(tempPos).getMaterial() == Material.AIR) {
                        if (canFitHitbox(tempPos)) blockPosList.add(tempPos);
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
        if (this.creature.world.getBlockState(pos.down()).getMaterial().isSolid()) {
            for (int x = -Math.round(width/2f); x <= Math.round(width/2f); x++) {
                for (int y = 0; y <= this.creature.height; y++) {
                    for (int z = -Math.round(width/2f); z <= Math.round(width/2f); z++) {
                        if (this.creature.world.getBlockState(pos.add(x, y, z)).getMaterial().isSolid()) return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
}
