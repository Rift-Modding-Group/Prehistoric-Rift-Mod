package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.TameBehaviorType;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.passive.EntityTameable;

public class RiftAttackForOwner extends EntityAIOwnerHurtTarget {
    private final RiftCreature creature;

    public RiftAttackForOwner(EntityTameable theEntityTameableIn) {
        super(theEntityTameableIn);
        this.creature = (RiftCreature) theEntityTameableIn;
    }

    @Override
    public boolean shouldExecute() {
        return !this.creature.isBeingRidden() && this.creature.getTameBehavior() == TameBehaviorType.ASSIST && super.shouldExecute();
    }
}
