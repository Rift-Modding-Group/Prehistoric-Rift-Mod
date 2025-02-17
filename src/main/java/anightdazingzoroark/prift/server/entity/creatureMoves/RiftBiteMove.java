package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;

public class RiftBiteMove extends RiftCreatureMove {
    public RiftBiteMove() {
        super(CreatureMove.BITE);
    }

    @Override
    public MovePriority canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return MovePriority.LOW;
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.removeSpeed();
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {

    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        if (target != null) user.attackEntityAsMob(target);
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.resetSpeed();
        if (user.isTamed()) user.energyActionMod++;
    }
}
