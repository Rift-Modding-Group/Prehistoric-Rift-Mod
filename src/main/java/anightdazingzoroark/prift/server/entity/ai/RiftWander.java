package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class RiftWander extends EntityAIWander {
    protected final RiftCreature creature;

    public RiftWander(RiftCreature creatureIn, double speedIn) {
        this(creatureIn, speedIn, 120);
    }

    public RiftWander(RiftCreature creatureIn, double speedIn, int chance) {
        super(creatureIn, speedIn, chance);
        this.creature = creatureIn;
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature.isSleeping()) return false;
        else if (this.creature.isTamed()) {
            if (this.creature instanceof RiftWaterCreature) {
                if (this.creature.getTameStatus() == TameStatusType.WANDER && !this.creature.isBeingRidden() && !this.creature.isInWater()) return super.shouldExecute();
                else return false;
            }
            else {
                if (this.creature.getTameStatus() == TameStatusType.WANDER && !this.creature.isBeingRidden()) return super.shouldExecute();
                else return false;
            }
        }
        else {
            if (this.creature instanceof RiftWaterCreature) {
                if (this.creature.isHerdLeader() && !this.creature.isInWater()) return super.shouldExecute();
                else if (!this.creature.isHerdLeader() && !this.creature.hasHerdLeader() && !this.creature.isInWater()) return super.shouldExecute();
                else return false;
            }
            else {
                if (this.creature.isHerdLeader()) return super.shouldExecute();
                else if (!this.creature.isHerdLeader() && !this.creature.hasHerdLeader()) return super.shouldExecute();
                else return false;
            }
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.creature instanceof RiftWaterCreature) return this.creature.getEnergy() > 0 && !this.creature.hasHerdLeader() && super.shouldContinueExecuting() && !this.creature.isInWater();
        return this.creature.getEnergy() > 0 && !this.creature.hasHerdLeader() && super.shouldContinueExecuting();
    }
}
