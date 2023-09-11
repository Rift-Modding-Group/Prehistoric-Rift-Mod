package anightdazingzoroark.rift.server.entity.ai;

import anightdazingzoroark.rift.server.entity.RiftCreature;
import anightdazingzoroark.rift.server.enums.TameStatusType;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIWander;

public class RiftWander extends EntityAIWander {
    private final RiftCreature creature;

    public RiftWander(EntityCreature creatureIn, double speedIn) {
        super(creatureIn, speedIn);
        this.creature = (RiftCreature) creatureIn;
    }

    @Override
    public boolean shouldExecute() {
        return (!this.creature.isTamed() || (this.creature.isTamed() && this.creature.getTameStatus() == TameStatusType.WANDER && !this.creature.isBeingRidden())) && super.shouldExecute();
    }
}
