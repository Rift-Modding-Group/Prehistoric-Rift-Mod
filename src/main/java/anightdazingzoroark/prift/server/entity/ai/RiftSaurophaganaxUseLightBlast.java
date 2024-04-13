package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.Saurophaganax;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftSaurophaganaxUseLightBlast extends EntityAIBase {
    protected final Saurophaganax saurophaganax;

    public RiftSaurophaganaxUseLightBlast(Saurophaganax saurophaganax) {
        this.saurophaganax = saurophaganax;
    }

    @Override
    public boolean shouldExecute() {
        return false;
    }
}
