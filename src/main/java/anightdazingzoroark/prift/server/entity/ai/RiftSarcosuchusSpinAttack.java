package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.Sarcosuchus;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftSarcosuchusSpinAttack extends EntityAIBase {
    private final Sarcosuchus sarcosuchus;

    public RiftSarcosuchusSpinAttack(Sarcosuchus sarcosuchus) {
        this.sarcosuchus = sarcosuchus;
    }

    @Override
    public boolean shouldExecute() {
        return false;
    }
}
