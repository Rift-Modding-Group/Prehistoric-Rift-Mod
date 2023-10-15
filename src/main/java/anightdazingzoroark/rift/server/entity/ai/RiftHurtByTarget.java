package anightdazingzoroark.rift.server.entity.ai;

import anightdazingzoroark.rift.server.entity.RiftCreature;
import anightdazingzoroark.rift.server.enums.TameBehaviorType;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIHurtByTarget;

public class RiftHurtByTarget extends EntityAIHurtByTarget {
    private final RiftCreature creature;

    public RiftHurtByTarget(EntityCreature creatureIn, boolean entityCallsForHelpIn) {
        super(creatureIn, entityCallsForHelpIn);
        this.setMutexBits(1);
        this.creature = (RiftCreature) creatureIn;
    }

    @Override
    public boolean shouldExecute() {
        if (!this.creature.isTamed()) {
            if (this.creature.getRevengeTarget() instanceof RiftCreature) {
                if (((RiftCreature)this.creature.getRevengeTarget()).creatureType == this.creature.creatureType) {
                    this.creature.setRevengeTarget(null);
                    return false;
                }
                else return super.shouldExecute();
            }
            else return super.shouldExecute();
        }
        else {
            if (this.creature.getTameBehavior() != TameBehaviorType.PASSIVE && !this.creature.isBeingRidden()) super.shouldExecute();
            else return false;
        }
        return false;
    }
}
