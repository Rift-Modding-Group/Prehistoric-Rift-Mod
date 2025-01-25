package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.projectile.ThrownStegoPlate;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class RiftPlateFlingMove extends RiftCreatureMove {
    public RiftPlateFlingMove() {
        super(CreatureMove.PLATE_FLING);
    }

    @Override
    public MovePriority canBeExecuted(RiftCreature user, EntityLivingBase target) {
        if (user.getDistance(target) > user.attackWidth() && user.getDistance(target) <= user.rangedWidth()) return MovePriority.HIGH;
        else if (user.getDistance(target) > user.rangedWidth()) {
            user.getNavigator().tryMoveToEntityLiving(target, 1.0D);
            user.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            return MovePriority.HIGH;
        }
        return MovePriority.NONE;
    }

    @Override
    public void onStartExecuting(RiftCreature user) {
        user.removeSpeed();
    }

    @Override
    public void whileExecuting(RiftCreature user) {

    }

    @Override
    public void onReachUsePoint(RiftCreature user, EntityLivingBase target, int useAmount) {
        if (user.getControllingPassenger() == null) this.shootEntityUnmounted(user, target);
        else this.shootEntityMounted(user);
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.resetSpeed();
    }

    @Override
    public void onHitEntity(RiftCreature user, EntityLivingBase target) {

    }

    @Override
    public void onHitBlock(RiftCreature user, BlockPos targetPos) {

    }

    private void shootEntityUnmounted(RiftCreature creature, EntityLivingBase target) {
        ThrownStegoPlate thrownStegoPlate = new ThrownStegoPlate(creature.world, creature);
        double d0 = target.posX - creature.posX;
        double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - thrownStegoPlate.posY;
        double d2 = target.posZ - creature.posZ;
        double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
        thrownStegoPlate.setVariant(creature.getVariant());
        thrownStegoPlate.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, 5F);
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
