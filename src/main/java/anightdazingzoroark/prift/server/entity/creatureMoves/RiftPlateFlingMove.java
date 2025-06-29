package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.projectile.ThrownStegoPlate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class RiftPlateFlingMove extends RiftCreatureMove {
    public RiftPlateFlingMove() {
        super(CreatureMove.PLATE_FLING);
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
        if (user.getControllingPassenger() == null) this.shootEntityUnmounted(user, target);
        else this.shootEntityMounted(user);
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
        user.enableCanRotateMounted();
    }

    private void shootEntityUnmounted(RiftCreature creature, Entity target) {
        ThrownStegoPlate thrownStegoPlate = new ThrownStegoPlate(creature.world, creature);
        double velX = target.posX - creature.posX;
        double velY = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - thrownStegoPlate.posY;
        double velZ = target.posZ - creature.posZ;
        double magnitude = MathHelper.sqrt(velX * velX + velZ * velZ);
        thrownStegoPlate.setVariant(creature.getVariant());
        thrownStegoPlate.shoot(velX, velY + magnitude * 0.20000000298023224D, velZ, 1.6F, 5F);
        thrownStegoPlate.setDamage(4D + (double)(creature.getLevel())/10D);
        creature.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (creature.getRNG().nextFloat() * 0.4F + 0.8F));
        creature.world.spawnEntity(thrownStegoPlate);
    }

    private void shootEntityMounted(RiftCreature user) {
        ThrownStegoPlate thrownStegoPlate = new ThrownStegoPlate(user.world, user, (EntityPlayer)user.getControllingPassenger());
        thrownStegoPlate.setDamage(4D + (double)(user.getLevel())/10D);
        thrownStegoPlate.setVariant(user.getVariant());
        thrownStegoPlate.shoot(user, user.rotationPitch, user.rotationYaw, 0.0F, 1.5F, 1.0F);
        user.world.spawnEntity(thrownStegoPlate);
    }
}
