package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.projectile.DilophosaurusSpit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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
    public void whileChargingUp(RiftCreature user) {

    }

    @Override
    public void whileExecuting(RiftCreature user) {

    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        if (user.getControllingPassenger() == null) this.shootEntityUnmounted(user, target);
        else this.shootEntityMounted(user);
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
        user.enableCanRotateMounted();
    }

    public void shootEntityUnmounted(RiftCreature user, Entity target) {
        DilophosaurusSpit dilophosaurusSpit = new DilophosaurusSpit(user.world, user);
        double d0 = target.posX - user.posX;
        double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 6.0F) - dilophosaurusSpit.posY;
        double d2 = target.posZ - user.posZ;
        double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
        dilophosaurusSpit.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.5F, 1.0F);
        dilophosaurusSpit.setDamage(2D + (double)(user.getLevel())/10D);
        user.world.spawnEntity(dilophosaurusSpit);
    }

    public void shootEntityMounted(RiftCreature user) {
        DilophosaurusSpit dilophosaurusSpit = new DilophosaurusSpit(user.world, user, (EntityPlayer)user.getControllingPassenger());
        dilophosaurusSpit.setDamage(2D + (double)(user.getLevel())/10D);
        dilophosaurusSpit.shoot(user, user.rotationPitch, user.rotationYaw, 0.0F, 1.5f, 1.0F);
        user.world.spawnEntity(dilophosaurusSpit);
    }
}
