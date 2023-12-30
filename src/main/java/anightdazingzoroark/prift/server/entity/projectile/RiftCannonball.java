package anightdazingzoroark.prift.server.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class RiftCannonball extends EntityArrow {
    private EntityLivingBase firer = null;

    public RiftCannonball(World worldIn) {
        super(worldIn);
    }

    public RiftCannonball(World world, EntityLivingBase firer, EntityPlayer user, double x, double y, double z) {
        this(world);
        this.setPosition(x, y, z);
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

    protected void onHit(RayTraceResult raytraceResultIn) {
//        Entity entity = raytraceResultIn.entityHit;

        if (!this.world.isRemote) {
//            if (entity != null && entity != this.cannon) {
//                result.entityHit.attackEntityFrom(DamageSource., 6.0F);
//                this.applyEnchantments(this.shootingEntity, result.entityHit);
//            }
            boolean flag = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this.firer);
            this.world.newExplosion((Entity)null, this.posX, this.posY, this.posZ, 6f, false, flag);
            this.setDead();
        }
        else super.onHit(raytraceResultIn);
    }

    public EntityLivingBase getFirer() {
        return this.firer;
    }

    @Override
    protected ItemStack getArrowStack() {
        return null;
    }
}
