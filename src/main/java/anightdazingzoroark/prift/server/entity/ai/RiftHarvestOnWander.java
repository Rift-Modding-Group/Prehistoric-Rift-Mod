package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IHarvestWhenWandering;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;

public class RiftHarvestOnWander extends EntityAIBase {
    private final RiftCreature creature;
    private IHarvestWhenWandering creatureHarvester;
    private BlockPos targetBlockPos;
    private int animTime;
    private final int harvestAnimLength;
    private final int harvestAnimTime;

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
        this.targetBlockPos = null;
    }

    @Override
    public void updateTask() {
        if (this.targetBlockPos != null) {
            //when not null, it will navigate towards target block
            //upon reaching target block, it will harvest
            if (RiftUtil.entityAtLocation(this.creature, this.targetBlockPos, this.creatureHarvester.harvestRange())) {
                if (this.animTime == 0) {
                    this.creatureHarvester.setHarvesting(true);
                }
                if (this.animTime == this.harvestAnimTime) {
                    this.creature.world.destroyBlock(this.targetBlockPos, true);
                    this.creature.setXP(this.creature.getXP() + 5);
                }
                if (this.animTime == this.harvestAnimLength) {
                    this.creatureHarvester.setHarvesting(false);
                    this.creature.energyActionMod++;
                }
                if (this.animTime > 30) {
                    this.animTime = -1;
                    this.creatureHarvester.setHarvesting(false);
                    this.targetBlockPos = null;
                }
                this.animTime++;
            }
            else this.creature.getMoveHelper().setMoveTo(this.targetBlockPos.getX(), this.targetBlockPos.getY(), this.targetBlockPos.getZ(), 1);
        }
        else {
            //when null, calculate point
            //when home position is set, it is to be within 16 blocks horizontally
            //and 7 blocks vertically of the home pos
            if (this.creature.getHasHomePos()) {
                int homePosMinX = this.creature.getHomePos().getX() - 16;
                int homePosMinY = this.creature.getHomePos().getY() - 7;
                int homePosMinZ = this.creature.getHomePos().getZ() - 16;
                main: for (int x = homePosMinX; x <= homePosMinX + 32; x++) {
                    for (int y = homePosMinY; y <= homePosMinY + 14; y++) {
                        for (int z = homePosMinZ; z <= homePosMinZ + 32; z++) {
                            BlockPos newPos = new BlockPos(x, y, z);
                            if (this.creatureHarvester.isValidBlockToHarvest(this.creature.world, newPos) && RiftUtil.blockExposedToAir(this.creature.world, newPos)) {
                                this.targetBlockPos = newPos;
                                break main;
                            }
                        }
                    }
                }
            }
        }
    }
}
