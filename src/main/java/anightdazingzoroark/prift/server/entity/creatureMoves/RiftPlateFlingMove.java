package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.projectile.RiftCreatureProjectile;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.projectile.RiftCreatureProjectileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class RiftPlateFlingMove extends RiftCreatureMove {
    private Entity targetToFlingTo;

    public RiftPlateFlingMove() {
        super(CreatureMove.PLATE_FLING);
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setCanMove(false);
        user.disableCanRotateMounted();
        if (user.isBeingRidden()) this.targetToFlingTo = target;
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
        RiftCreatureProjectileEntity thrownStegoPlate = RiftCreatureProjectile.createCreatureProjectile(RiftCreatureProjectile.Enum.THROWN_STEGOSAURUS_PLATE, creature);
        double velX = target.posX - creature.posX;
        double velY = (target.getEntityBoundingBox().minY + target.getEntityBoundingBox().maxY) / 2 - thrownStegoPlate.posY;
        double velZ = target.posZ - creature.posZ;
        double magnitude = MathHelper.sqrt(velX * velX + velZ * velZ);
        thrownStegoPlate.shoot(velX, velY + magnitude * 0.20000000298023224D, velZ, 1.4F, 5F);
        creature.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (creature.getRNG().nextFloat() * 0.4F + 0.8F));
        creature.world.spawnEntity(thrownStegoPlate);
    }

    private void shootEntityMounted(RiftCreature user) {
        RiftCreatureProjectileEntity thrownStegoPlate = RiftCreatureProjectile.createCreatureProjectile(RiftCreatureProjectile.Enum.THROWN_STEGOSAURUS_PLATE, user);
        if (this.targetToFlingTo != null) {
            double velX = this.targetToFlingTo.posX - user.posX;
            double velY = (this.targetToFlingTo.posY + this.targetToFlingTo.getEntityBoundingBox().maxY) / 2 - thrownStegoPlate.posY;
            double velZ = this.targetToFlingTo.posZ - user.posZ;
            double magnitude = MathHelper.sqrt(velX * velX + velZ * velZ);
            thrownStegoPlate.shoot(velX, velY + magnitude * 0.20000000298023224D, velZ, 1.4F, 1f);
        }
        else thrownStegoPlate.shoot(user, user.rotationPitch, user.rotationYaw, 0f, 1.4F, 1f);
        user.world.spawnEntity(thrownStegoPlate);
    }
}
