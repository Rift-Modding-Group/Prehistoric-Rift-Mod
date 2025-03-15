package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;

public class RiftDeathRollMove extends RiftCreatureMove {
    public RiftDeathRollMove() {
        super(CreatureMove.DEATH_ROLL);
    }

    @Override
    public MovePriority canBeExecutedUnmounted(RiftCreature user, Entity target) {
        if (user.getDistance(target) > user.attackWidth() + 1 && user.getDistance(target) <= user.rangedWidth()) {
            return MovePriority.NONE;
        }
        return MovePriority.HIGH;
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setGrabVictim(null);
        user.removeSpeed();
    }

    @Override
    public void whileChargingUp(RiftCreature user) {

    }

    @Override
    public void whileExecuting(RiftCreature user) {
        if (user.getGrabVictim() != null && user.getGrabVictim().isEntityAlive()) user.attackEntityAsMobWithMultiplier(user.getGrabVictim(), 0.25f);
        else this.forceStopFlag = true;
    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        user.setGrabVictim(target);
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setGrabVictim(null);
        user.resetSpeed();
    }
}
