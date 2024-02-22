package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIWander;

public class RiftWander extends EntityAIWander {
    private final RiftCreature creature;

    public RiftWander(EntityCreature creatureIn, double speedIn) {
        this(creatureIn, speedIn, 120);
    }

    public RiftWander(EntityCreature creatureIn, double speedIn, int chance) {
        super(creatureIn, speedIn, chance);
        this.creature = (RiftCreature) creatureIn;
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature.isTamed()) {
            if (this.creature.getTameStatus() == TameStatusType.WANDER && !this.creature.isBeingRidden()) return super.shouldExecute();
            else return false;
        }
        return super.shouldExecute();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.creature.getEnergy() > 0 && super.shouldContinueExecuting();
    }
}
