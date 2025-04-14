package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;

public class RiftGrabMove extends RiftCreatureMove {
    public RiftGrabMove() {
        super(CreatureMove.GRAB);
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
        if (user.getGrabVictim() != null) user.setGrabVictim(null);
        else user.setGrabVictim(target);
    }

    @Override
    public void onStopExecuting(RiftCreature user) {

    }
}
