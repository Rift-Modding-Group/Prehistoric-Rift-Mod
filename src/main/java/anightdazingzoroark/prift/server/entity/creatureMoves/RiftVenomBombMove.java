package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.projectile.RiftCreatureProjectile;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.projectile.RiftCreatureProjectileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class RiftVenomBombMove extends RiftCreatureMove {
    public RiftVenomBombMove() {
        super(CreatureMove.VENOM_BOMB);
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
        if (user.getControllingPassenger() == null) this.shootProjectileUnmounted(user, target, useAmount, RiftCreatureProjectile.Enum.VENOM_BOMB);
        else this.shootProjectileMounted(user, target, useAmount, RiftCreatureProjectile.Enum.VENOM_BOMB);
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
        user.enableCanRotateMounted();
    }
}
