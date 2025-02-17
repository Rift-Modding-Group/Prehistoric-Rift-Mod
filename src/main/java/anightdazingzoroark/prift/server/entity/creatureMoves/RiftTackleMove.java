package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;

public class RiftTackleMove extends RiftCreatureMove {
    public RiftTackleMove() {
        super(CreatureMove.TACKLE);
    }

    @Override
    public MovePriority canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return MovePriority.LOW;
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {

    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {

    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {}

    @Override
    public void onStopExecuting(RiftCreature user) {

    }
}
