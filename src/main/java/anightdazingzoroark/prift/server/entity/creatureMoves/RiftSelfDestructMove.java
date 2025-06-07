package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;

public class RiftSelfDestructMove extends RiftCreatureMove {
    public RiftSelfDestructMove() {
        super(CreatureMove.SELF_DESTRUCT);
    }

    @Override
    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return user.world.rand.nextInt(4) == 0;
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setCanMove(false);
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {}

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        user.world.createExplosion(user, user.posX, user.posY, user.posZ, 8f, true);
        user.onKillCommand();
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
    }
}
