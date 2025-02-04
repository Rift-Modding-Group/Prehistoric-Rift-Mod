package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;

public abstract class RiftCreatureMove {
    public final CreatureMove creatureMove;
    public boolean forceStopFlag = false;
    protected int useValue;

    public RiftCreatureMove(CreatureMove creatureMove) {
        this.creatureMove = creatureMove;
    }

    //if return false, its low priority
    //if return true, its high priority
    public abstract MovePriority canBeExecuted(RiftCreature user, EntityLivingBase target);

    public void onStartExecuting(RiftCreature user) {
        this.onStartExecuting(user, null);
    }

    public abstract void onStartExecuting(RiftCreature user, EntityLivingBase target);

    public void onEndChargeUp(RiftCreature user, int useAmount) {}

    public abstract void whileExecuting(RiftCreature user);

    public void onReachUsePoint(RiftCreature user, EntityLivingBase target) {
        this.onReachUsePoint(user, target, 0);
    }

    public abstract void onReachUsePoint(RiftCreature user, EntityLivingBase target, int useAmount);

    public abstract void onStopExecuting(RiftCreature user);

    //executes when the attack hits an entity
    public abstract void onHitEntity(RiftCreature user, EntityLivingBase target);

    //executes when the attack hits a block
    public abstract void onHitBlock(RiftCreature user, BlockPos targetPos);

    public void lookAtTarget(RiftCreature user, EntityLivingBase target) {
        if (target != null) user.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
    }

    public boolean hasChargeBar() {
        return this.creatureMove.chargeType != CreatureMove.ChargeType.NONE;
    }

    public void setUseValue(int value) {
        this.useValue = value;
    }

    public int getUseValue() {
        return this.useValue;
    }

    public enum MovePriority {
        HIGH, //move is put at top of list
        LOW, //move is put at bottom of list
        NONE; //move cannot be used at all
    }
}
