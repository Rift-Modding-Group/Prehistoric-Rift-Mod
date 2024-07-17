package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.Palaeocastor;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftPalaeocastorDig extends EntityAIBase {
    private final Palaeocastor palaeocastor;

    public RiftPalaeocastorDig(Palaeocastor palaeocastor) {
        this.palaeocastor = palaeocastor;
    }

    @Override
    public boolean shouldExecute() {
        return false;
    }
}
