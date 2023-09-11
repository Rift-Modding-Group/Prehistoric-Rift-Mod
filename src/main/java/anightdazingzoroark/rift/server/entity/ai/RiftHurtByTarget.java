package anightdazingzoroark.rift.server.entity.ai;

import anightdazingzoroark.rift.server.entity.RiftCreature;
import anightdazingzoroark.rift.server.enums.TameBehaviorType;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIHurtByTarget;

public class RiftHurtByTarget extends EntityAIHurtByTarget {
    private final RiftCreature creature;

    public RiftHurtByTarget(EntityCreature creatureIn, boolean entityCallsForHelpIn) {
        super(creatureIn, entityCallsForHelpIn, new Class[0]);
        this.creature = (RiftCreature) creatureIn;
    }

    public boolean shouldExecute() {
        return (!this.creature.isTamed() || (this.creature.isTamed() && this.creature.getTameBehavior() != TameBehaviorType.PASSIVE && !this.creature.isBeingRidden())) && super.shouldExecute();
    }
}
