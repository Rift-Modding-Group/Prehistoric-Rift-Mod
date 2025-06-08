package anightdazingzoroark.prift.server.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class RiftProjectile extends EntityArrow implements IProjectile {
    private EntityPlayer rider;

    public RiftProjectile(World worldIn) {
        super(worldIn);
    }

    public RiftProjectile(World world, double x, double y, double z) {
        this(world);
        this.setPosition(x, y, z);
    }

    public RiftProjectile(World world, EntityLivingBase shooter) {
        this(world, shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.1, shooter.posZ);
        this.shootingEntity = shooter;
    }

    public RiftProjectile(World world, EntityLivingBase shooter, EntityPlayer rider) {
        this(world, rider.posX, rider.posY + rider.getEyeHeight() - 0.1, rider.posZ);
        this.shootingEntity = shooter;
        this.rider = rider;
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

    //this is temporary, when riftlib adds support for projectiles this workaround will be removed
    public abstract ItemStack getItemToRender();

    @Override
    protected ItemStack getArrowStack() {
        return null;
    }
}
