package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class RiftLeechMove extends RiftCreatureMove {
    public RiftLeechMove() {
        super(CreatureMove.LEECH);
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
        if (target != null) {
            float oldTargetHealth = 0;
            if (target instanceof EntityLivingBase) oldTargetHealth = ((EntityLivingBase)target).getHealth();

            user.attackEntityAsMob(target);

            float newTargetHealth = 0;
            if (target instanceof EntityLivingBase) newTargetHealth = ((EntityLivingBase)target).getHealth();

            user.heal((float) Math.ceil((oldTargetHealth - newTargetHealth) / 2D));
        }
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
    }
}
