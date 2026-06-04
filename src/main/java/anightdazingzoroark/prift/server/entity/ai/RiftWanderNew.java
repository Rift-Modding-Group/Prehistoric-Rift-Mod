package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.ai.EntityAIWander;

public class RiftWanderNew extends EntityAIWander {
    private final RiftCreature creature;

    public RiftWanderNew(RiftCreature creature, double speedIn) {
        super(creature, speedIn);
        this.creature = creature;
    }
}
