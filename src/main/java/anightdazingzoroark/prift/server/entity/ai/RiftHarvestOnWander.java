package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IHarvestWhenWandering;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RiftHarvestOnWander extends EntityAIBase {
    private final RiftCreature creature;
    private IHarvestWhenWandering creatureHarvester;
    private BlockPos targetBlockPos;
    private int animTime;
    private final int harvestAnimLength;
    private final int harvestAnimTime;
    private Path path;

    public RiftHarvestOnWander(RiftCreature creature, float harvestAnimLength, float harvestAnimTime) {
        this.creature = creature;
        this.harvestAnimLength = (int)(20f * harvestAnimLength);
        this.harvestAnimTime = (int)(20f * harvestAnimTime);
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        return this.creature.isTamed() && this.creature.getTameStatus() == TameStatusType.WANDER && this.creature instanceof IHarvestWhenWandering;
    }

    @Override
    public void startExecuting() {
        this.creatureHarvester = (IHarvestWhenWandering) this.creature;
    }

    @Override
    public void resetTask() {
        this.animTime = 0;
        this.creature.getNavigator().clearPath();
        this.targetBlockPos = null;
        this.path = null;
    }

    @Override
    public void updateTask() {
        if (this.path != null) {
            //when not null, it will navigate towards target block
            //upon reaching target block, it will harvest
            if (this.path.isFinished()) {
                if (this.animTime == 0) {
                    this.creatureHarvester.setHarvesting(true);
                }
                if (this.animTime == this.harvestAnimTime) {
                    //this.creature.world.destroyBlock(this.targetBlockPos, true);
                    this.creatureHarvester.harvestBlock(this.targetBlockPos);
                    this.creature.setXP(this.creature.getXP() + 5);
                }
                if (this.animTime == this.harvestAnimLength) {
                    this.creatureHarvester.setHarvesting(false);
                    this.creature.energyActionMod++;
                }
                if (this.animTime > 30) {
                    this.animTime = -1;
                    this.creatureHarvester.setHarvesting(false);
                    this.creature.getNavigator().clearPath();
                    this.targetBlockPos = null;
                    this.path = null;
                }
                this.animTime++;
            }
        }
        else {
            //when null, calculate point
            //when home position is set, it is to be within 16 blocks horizontally
            //and 7 blocks vertically of the home pos
            if (this.creature.getHasHomePos()) {
                BlockPos obtainedPos = this.calculatePos();
                if (obtainedPos != null) {
                    this.path = this.creature.getNavigator().getPathToPos(obtainedPos);
                    this.creature.getNavigator().setPath(this.path, 1D);
                    this.targetBlockPos = obtainedPos;
                }
            }
        }
    }

    private BlockPos calculatePos() {
        int homePosMinX = this.creature.getHomePos().getX() - 16;
        int homePosMinY = this.creature.getHomePos().getY() - 7;
        int homePosMinZ = this.creature.getHomePos().getZ() - 16;
        for (int x = homePosMinX; x <= homePosMinX + 32; x++) {
            for (int y = homePosMinY; y <= homePosMinY + 14; y++) {
                for (int z = homePosMinZ; z <= homePosMinZ + 32; z++) {
                    BlockPos newPos = new BlockPos(x, y, z);
                    if (this.creatureHarvester.isValidBlockToHarvest(this.creature.world, newPos) && this.blockExposedToAir(this.creature.world, newPos)) {
                        Path testPath = this.creature.getNavigator().getPathToPos(newPos);
                        if (testPath != null && testPath.getFinalPathPoint() != null) {
                            BlockPos finalTestPos = new BlockPos(testPath.getFinalPathPoint().x, testPath.getFinalPathPoint().y, testPath.getFinalPathPoint().z);
                            if (this.blockPosClose(finalTestPos, newPos)) return newPos;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean blockPosClose(BlockPos blockPos1, BlockPos blockPos2) {
        //blockPos1 is for the final path point
        //blockPos2 is for the block that will be mined
        double dist = blockPos1.getDistance(blockPos2.getX(), blockPos2.getY(), blockPos2.getZ());
        return dist <= this.creatureHarvester.harvestRange();
    }

    private boolean blockExposedToAir(World world, BlockPos pos) {
        for (int x = -1; x <= 1 ; x++) {
            for (int y = -1; y <= 1 ; y++) {
                for (int z = -1; z <= 1 ; z++) {
                    BlockPos newPos = pos.add(x, y, z);
                    if (world.getBlockState(newPos).getMaterial() == Material.AIR) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
