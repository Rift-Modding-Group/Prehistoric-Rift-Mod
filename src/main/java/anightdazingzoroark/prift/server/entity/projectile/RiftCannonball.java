package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.projectile.RiftLibProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class RiftCannonball extends RiftLibProjectile {
    private EntityLivingBase firer = null;

    public RiftCannonball(World worldIn) {
        super(worldIn);
    }

    public RiftCannonball(World world, EntityLivingBase firer, EntityPlayer user) {
        this(world);
        this.setPosition(user.posX, user.posY + user.getEyeHeight() - 0.1, user.posZ);
        this.shootingEntity = user;
        this.firer = firer;
    }

    public void shoot(Entity shooter, float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy) {
        float f = -MathHelper.sin(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
        float f1 = -MathHelper.sin((rotationPitchIn + pitchOffset) * 0.017453292F);
        float f2 = MathHelper.cos(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
        this.shoot((double)f, (double)f1, (double)f2, velocity, inaccuracy);
        this.motionX += shooter.motionX;
        this.motionZ += shooter.motionZ;

        if (!shooter.onGround) this.motionY += shooter.motionY;
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
    public boolean canRotateVertically() {
        return false;
    }

    @Override
    public void registerControllers(AnimationData animationData) {}
}
