package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class RiftKickMove extends RiftCreatureMove {
    public RiftKickMove() {
        super(CreatureMove.KICK);
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.removeSpeed();
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {}

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        if (target != null) {
            //damage target
            user.attackEntityAsMobWithMultiplier(target, 0.25f);

            //knock back target
            double d0 = user.posX - target.posX;
            double d1 = user.posZ - target.posZ;
            double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
            if (target instanceof EntityLivingBase) ((EntityLivingBase)target).knockBack(user, 2f, d0 / d2 * 8.0D, d1 / d2 * 8.0D);
        }
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.resetSpeed();
    }
}
