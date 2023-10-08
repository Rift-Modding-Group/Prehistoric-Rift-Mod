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
        if (this.creature.isTamed()) {
            if (this.creature.getTameStatus() == TameStatusType.WANDER && !this.creature.isBeingRidden()) return super.shouldExecute();
            else return false;
        }
        else if (this.creature.canDoHerding() && this.creature.isHerdLeader()) return super.shouldExecute();
        else if (!this.creature.canDoHerding()) return super.shouldExecute();
        return false;
    }
}
