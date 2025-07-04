package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;

public class RiftGnashMove extends RiftCreatureMove {
    public RiftGnashMove() {
        super(CreatureMove.GNASH);
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setGrabVictim(null);
        user.setCanMove(false);
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
        user.setCanMove(true);
    }
}
