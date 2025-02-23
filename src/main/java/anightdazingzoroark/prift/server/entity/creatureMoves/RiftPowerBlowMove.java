package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.Parasaurolophus;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;

public class RiftPowerBlowMove extends RiftCreatureMove {
    public RiftPowerBlowMove() {
        super(CreatureMove.POWER_BLOW);
    }

    @Override
    public MovePriority canBeExecutedUnmounted(RiftCreature user, Entity target) {
        if (user instanceof Parasaurolophus) return MovePriority.HIGH;
        else {
            if (user.getDistance(target) > user.attackWidth() + 1 && user.getDistance(target) <= user.rangedWidth()) {
                return MovePriority.HIGH;
            }
            return MovePriority.NONE;
        }
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
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {}

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.resetSpeed();
    }

    @Override
    public int[] unmountedChargeBounds() {
        return new int[]{0, (int)(this.creatureMove.maxUse * 0.3)};
    }
}
