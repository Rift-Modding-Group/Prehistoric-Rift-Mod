package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;

public abstract class RiftCreatureMove {
    public final CreatureMove creatureMove;
    public boolean forceStopFlag = false;
    protected int useValue;

    public RiftCreatureMove(CreatureMove creatureMove) {
        this.creatureMove = creatureMove;
    }

    //if return false, its low priority
    //if return true, its high priority
    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        return true;
    }

    public boolean canBeExecutedMounted(RiftCreature user, Entity target) {
        return true;
    }

    public String cannotExecuteMountedMessage() {
        return null;
    }

    public void onStartExecuting(RiftCreature user) {
        this.onStartExecuting(user, null);
    }

    public abstract void onStartExecuting(RiftCreature user, Entity target);

    public void onEndChargeUp(RiftCreature user, int useAmount) {}

    public abstract void whileChargingUp(RiftCreature user);

    public abstract void whileExecuting(RiftCreature user);

    public void onReachUsePoint(RiftCreature user, Entity target) {
        this.onReachUsePoint(user, target, 0);
    }

    public abstract void onReachUsePoint(RiftCreature user, Entity target, int useAmount);

    public abstract void onStopExecuting(RiftCreature user);

    public void lookAtTarget(RiftCreature user, Entity target) {
        if (target != null) user.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
    }

    //this is for advanced anim time controls when managing animations for moves used while creature is mounted
    //the use value of the move is set here
    public void setUseValue(int value) {
        this.useValue = value;
    }

    //if the move is disrupted, the use value left goes here to be subtracted from the anim times
    public int getUseValue() {
        return this.useValue;
    }

    public int[] unmountedChargeBounds() {
        return new int[]{(int)(this.creatureMove.maxUse * 0.3), this.creatureMove.maxUse};
    }
}
