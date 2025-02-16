package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;

public class RiftBideMove extends RiftCreatureMove {
    public RiftBideMove() {
        super(CreatureMove.BIDE);
    }

    @Override
    public MovePriority canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return MovePriority.LOW;
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {

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
