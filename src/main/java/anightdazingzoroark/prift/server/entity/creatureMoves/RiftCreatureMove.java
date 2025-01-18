package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;

public abstract class RiftCreatureMove {
    public final CreatureMove creatureMove;
    public final int animTotalLength; //this is in ticks
    public final double animPercentOnUse;

    public RiftCreatureMove(CreatureMove creatureMove, int animTotalLength, double animPercentOnUse) {
        this.creatureMove = creatureMove;
        this.animTotalLength = animTotalLength; //this is in ticks
        this.animPercentOnUse = animPercentOnUse;
    }

    //if return false, its low priority
    //if return true, its high priority
    public abstract MovePriority canBeExecuted(RiftCreature user, EntityLivingBase target);

    public abstract void onStartExecuting(RiftCreature user);

    public abstract void whileExecuting(RiftCreature user);

    public abstract void onReachUsePoint(RiftCreature user, EntityLivingBase target);

    public abstract void onStopExecuting(RiftCreature user);

    //executes when the attack hits an entity
    public abstract void onHitEntity(RiftCreature user, EntityLivingBase target);

    //executes when the attack hits a block
    public abstract void onHitBlock(RiftCreature user, BlockPos targetPos);

    public boolean hasChargeBar() {
        return this.creatureMove.chargeType != CreatureMove.ChargeType.NONE;
    }

    public enum MovePriority {
        HIGH, //move is put at top of list
        LOW, //move is put at bottom of list
        NONE; //move cannot be used at all
    }
}
