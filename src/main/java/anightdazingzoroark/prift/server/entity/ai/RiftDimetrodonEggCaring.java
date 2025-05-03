package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.Dimetrodon;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftDimetrodonEggCaring extends EntityAIBase {
    private final Dimetrodon dimetrodon;

    public RiftDimetrodonEggCaring(Dimetrodon dimetrodon) {
        this.dimetrodon = dimetrodon;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        return this.dimetrodon.isTakingCareOfEgg() && this.dimetrodon.isSitting();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.dimetrodon.eggTarget != null && this.dimetrodon.eggTarget.isEntityAlive();
    }

    @Override
    public void resetTask() {
        this.dimetrodon.setSitting(false);
        this.dimetrodon.setTakingCareOfEgg(false);
        this.dimetrodon.eggTarget = null;
    }
}
