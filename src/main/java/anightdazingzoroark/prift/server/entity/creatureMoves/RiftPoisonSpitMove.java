package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.projectile.RiftCreatureProjectile;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.projectile.RiftCreatureProjectileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class RiftPoisonSpitMove extends RiftCreatureMove {
    public RiftPoisonSpitMove() {
        super(CreatureMove.POISON_SPIT);
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setCanMove(false);
        user.disableCanRotateMounted();
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {}

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        if (user.getControllingPassenger() == null) this.shootProjectileUnmounted(user, target, useAmount, RiftCreatureProjectile.Enum.POISON_SPIT);
        else this.shootProjectileMounted(user, target, useAmount, RiftCreatureProjectile.Enum.POISON_SPIT);
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
        user.enableCanRotateMounted();
    }
}
