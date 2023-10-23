package anightdazingzoroark.rift.server.entity.ai;

import anightdazingzoroark.rift.server.entity.creature.RiftCreature;
import anightdazingzoroark.rift.server.enums.TameBehaviorType;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.passive.EntityTameable;

public class RiftProtectOwner extends EntityAIOwnerHurtByTarget {
    private final RiftCreature creature;

    public RiftProtectOwner(EntityTameable theDefendingTameableIn) {
        super(theDefendingTameableIn);
        this.creature = (RiftCreature) theDefendingTameableIn;
    }

    @Override
    public boolean shouldExecute() {
        return !this.creature.isBeingRidden() && (this.creature.getTameBehavior() == TameBehaviorType.ASSIST || this.creature.getTameBehavior() == TameBehaviorType.NEUTRAL) && super.shouldExecute();
    }
}
