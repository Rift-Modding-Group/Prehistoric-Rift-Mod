package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;

public class RiftDeathRollMove extends RiftCreatureMove {
    public RiftDeathRollMove() {
        super(CreatureMove.DEATH_ROLL);
    }

    @Override
    public MovePriority canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return null;
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {

    }

    @Override
    public void whileChargingUp(RiftCreature user) {

    }

    @Override
    public void whileExecuting(RiftCreature user) {

    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {

    }

    @Override
    public void onStopExecuting(RiftCreature user) {

    }
}
