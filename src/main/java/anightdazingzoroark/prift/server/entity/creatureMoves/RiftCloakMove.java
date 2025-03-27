package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;

public class RiftCloakMove extends RiftCreatureMove {
    public RiftCloakMove() {
        super(CreatureMove.CLOAK);
    }

    @Override
    public MovePriority canBeExecutedUnmounted(RiftCreature user, Entity target) {
        if (user.canUtilizeCloaking() && !user.isCloaked() && user.getRevengeTarget() == null && user.getAttackTarget() == null
                && user.getGrabVictim() == null) return MovePriority.HIGH;
        return MovePriority.NONE;
    }

    @Override
    public boolean canBeExecutedMounted(RiftCreature user, Entity target) {
        return user.canUtilizeCloaking();
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.removeSpeed();
    }

    @Override
    public void whileChargingUp(RiftCreature user) {

    }

    @Override
    public void whileExecuting(RiftCreature user) {

    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        user.setCloaked(!user.isCloaked());
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.resetSpeed();
    }
}
