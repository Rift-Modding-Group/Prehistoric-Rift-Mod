package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.ai.EntityAIWander;

public class RiftWander extends EntityAIWander {
    private final RiftCreature creature;

    public RiftWander(RiftCreature creature) {
        super(creature, 1D);
        this.creature = creature;
    }

    @Override
    public boolean shouldExecute() {
        return this.creature.canLeadHerdBehavior() && super.shouldExecute();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.creature.canLeadHerdBehavior() && super.shouldContinueExecuting();
    }

    @Override
    public void resetTask() {
        if (!this.creature.canLeadHerdBehavior()) this.creature.getNavigator().clearPath();
        super.resetTask();
    }
}
