package anightdazingzoroark.prift.server.entity.aiNew;

import anightdazingzoroark.prift.server.entity.creaturenew.RiftCreatureNew;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftUnmountedUseMoveNew extends EntityAIBase {
    private final RiftCreatureNew creature;

    public RiftUnmountedUseMoveNew(RiftCreatureNew creature) {
        this.creature = creature;
    }

    @Override
    public boolean shouldExecute() {
        return false;
    }
}
