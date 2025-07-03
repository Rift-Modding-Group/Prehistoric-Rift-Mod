package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IRiftProjectile;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.projectile.RiftLibProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class RiftMortarShell extends RiftLibProjectile {
    private EntityLivingBase firer = null;

    public RiftMortarShell(World worldIn) {
        super(worldIn);
    }

    public RiftMortarShell(World world, EntityLivingBase firer, EntityPlayer user) {
        this(world);
        this.setPosition(firer.posX, firer.posY + 2.5, firer.posZ);
        this.shootingEntity = user;
        this.firer = firer;
    }

    public void shoot(Entity shooter, int launchDist) {
        double angleYawRad = Math.toRadians(shooter.rotationYaw);
        double dx = Math.sin(angleYawRad) * -launchDist;
        double dz = Math.cos(angleYawRad) * launchDist;
        double velY = Math.sqrt(2 * RiftUtil.gravity * launchDist);

        double dist = Math.sqrt(dx * dx + dz * dz);
        double totalTime = velY / RiftUtil.gravity;
        double velXZ = dist / totalTime / 2;

        double angleToTarget = Math.atan2(dz, dx);

        this.motionX = velXZ * Math.cos(angleToTarget);
        this.motionZ = velXZ * Math.sin(angleToTarget);
        this.motionY = velY;
    }

    @Override
    public void projectileEntityEffects(EntityLivingBase entityLivingBase) {
        boolean flag = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this.firer);
        this.world.newExplosion(this.firer, this.posX, this.posY, this.posZ, 6f, false, flag);
    }

    @Override
    public double getDamage() {
        return 0;
    }

    @Override
    public SoundEvent getOnProjectileHitSound() {
        return null;
    }

    @Override
    public void registerControllers(AnimationData animationData) {}
}
