package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftSleepAtDay extends EntityAIBase {
    private final RiftCreature creature;

    public RiftSleepAtDay(RiftCreature creature) {
        this.creature = creature;
        this.setMutexBits(10);
    }

    @Override
    public boolean shouldExecute() {
        return this.creature.world.isDaytime() && this.creature.getAttackTarget() == null && !this.creature.isInCave() && !this.creature.isInWater();
    }

    @Override
    public void startExecuting() {
        this.creature.setSleeping(true);
        this.creature.getNavigator().clearPath();
        this.creature.setTameProgress(0);
    }

    @Override
    public void resetTask() {
        this.creature.setSleeping(false);
    }
}
