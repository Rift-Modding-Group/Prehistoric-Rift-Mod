package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.Parasaurolophus;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftParasaurolophusControlledBlow extends EntityAIBase {
    private Parasaurolophus parasaurolophus;

    public RiftParasaurolophusControlledBlow(Parasaurolophus parasaurolophus) {
        this.parasaurolophus = parasaurolophus;
    }

    @Override
    public boolean shouldExecute() {
        return this.parasaurolophus.isBeingRidden() && this.parasaurolophus.isBlowing();
    }
}
