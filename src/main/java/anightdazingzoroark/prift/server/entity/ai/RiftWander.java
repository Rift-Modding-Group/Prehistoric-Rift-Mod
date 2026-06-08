package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.ai.EntityAIWander;

public class RiftWander extends EntityAIWander {
    private final RiftCreature creature;

    public RiftWander(RiftCreature creature, double speedIn) {
        super(creature, speedIn);
        this.creature = creature;
    }
}
