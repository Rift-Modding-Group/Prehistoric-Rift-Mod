package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.projectile.RiftCreatureProjectile;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.projectile.RiftCreatureProjectileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class RiftPlateScatterMove extends RiftCreatureMove {
    public RiftPlateScatterMove() {
        super(CreatureMove.PLATE_SCATTER);
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
        for (int angle = - 45; angle <= 45; angle += 15) {
            RiftCreatureProjectileEntity thrownStegoPlate = RiftCreatureProjectile.createCreatureProjectile(RiftCreatureProjectile.Enum.THROWN_STEGOSAURUS_PLATE, creature);
            double velX = target.posX - creature.posX;
            double velY = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - thrownStegoPlate.posY;
            double velZ = target.posZ - creature.posZ;
            double magnitude = MathHelper.sqrt(velX * velX + velZ * velZ);
            double targetAngle = Math.atan2(velZ, velX);

            double newVelX = magnitude * Math.sin((targetAngle + Math.toRadians(angle)));
            double newVelZ = magnitude * Math.cos(-(targetAngle + Math.toRadians(angle)));

            thrownStegoPlate.shoot(newVelX, velY + magnitude * 0.20000000298023224D, newVelZ, 1.6F, 5F);
            thrownStegoPlate.setDamage(8D + (double)(creature.getLevel())/5D);
            creature.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (creature.getRNG().nextFloat() * 0.4F + 0.8F));
            creature.world.spawnEntity(thrownStegoPlate);
        }
    }

    private void shootEntityMounted(RiftCreature user) {
        for (int angle = - 45; angle <= 45; angle += 15) {
            RiftCreatureProjectileEntity thrownStegoPlate = RiftCreatureProjectile.createCreatureProjectile(RiftCreatureProjectile.Enum.THROWN_STEGOSAURUS_PLATE, user);
            thrownStegoPlate.shoot(user, user.rotationPitch, user.rotationYaw + angle, 0.0F, 1.5F, 1.0F);
            user.world.spawnEntity(thrownStegoPlate);
        }
    }
}
