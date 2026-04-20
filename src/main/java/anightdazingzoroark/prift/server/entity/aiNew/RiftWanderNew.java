package anightdazingzoroark.prift.server.entity.aiNew;

import anightdazingzoroark.prift.server.entity.creaturenew.RiftCreatureNew;
import net.minecraft.entity.ai.EntityAIWander;

public class RiftWanderNew extends EntityAIWander {
    private final RiftCreatureNew creature;

    public RiftWanderNew(RiftCreatureNew creature, double speedIn) {
        super(creature, speedIn);
        this.creature = creature;
    }
}
