package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.server.entity.largeWeapons.RiftCannon;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftCatapult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class RiftCannonball extends EntityArrow {
    private RiftCannon cannon;

    public RiftCannonball(World worldIn) {
        super(worldIn);
    }

    public RiftCannonball(World world, double x, double y, double z) {
        this(world);
        this.setPosition(x, y, z);
    }

    public RiftCannonball(World world, RiftCannon cannon, EntityPlayer user) {
        this(world, user.posX, user.posY, user.posZ);
        this.shootingEntity = user;
        this.cannon = cannon;
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
        Entity entity = raytraceResultIn.entityHit;

        if (!this.world.isRemote) {
            if (entity != null && entity != this.cannon) {
                float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                int i = MathHelper.ceil((double) f * this.getDamage());

                if (this.getIsCritical()) i += this.rand.nextInt(i / 2 + 2);

                DamageSource damagesource;

                if (this.shootingEntity == null) damagesource = DamageSource.causeArrowDamage(this, this);
                else damagesource = DamageSource.causeArrowDamage(this, this.shootingEntity);

                if (entity.attackEntityFrom(damagesource, (float) i)) {
                    if (entity instanceof EntityLivingBase) {
                        EntityLivingBase entitylivingbase = (EntityLivingBase) entity;
                        this.arrowHit(entitylivingbase);
                    }
                    this.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                }
                else {
                    this.motionX *= -0.10000000149011612D;
                    this.motionY *= -0.10000000149011612D;
                    this.motionZ *= -0.10000000149011612D;
                    this.rotationYaw += 180.0F;
                    this.prevRotationYaw += 180.0F;
                    if (this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ < 0.0010000000474974513D) {
                        this.setDead();
                    }
                }
            }
        }
        else super.onHit(raytraceResultIn);
    }

    @Override
    protected ItemStack getArrowStack() {
        return null;
    }
}
