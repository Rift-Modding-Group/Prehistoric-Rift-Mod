package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.projectile.RiftCreatureProjectile;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;

public class RiftPowerBlowMove extends RiftCreatureMove {
    public RiftPowerBlowMove() {
        super(CreatureMove.POWER_BLOW);
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
        if (user.getControllingPassenger() == null) this.shootProjectileUnmounted(user, target, useAmount, RiftCreatureProjectile.POWER_BLOW);
        else this.shootProjectileMounted(user, target, useAmount, RiftCreatureProjectile.POWER_BLOW);
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
        user.enableCanRotateMounted();
    }

    @Override
    public int[] unmountedChargeBounds() {
        return new int[]{0, (int)(this.creatureMove.maxUse * 0.3)};
    }
}
