package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreaturePart;
import anightdazingzoroark.prift.server.entity.interfaces.IRiftProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class Mudball extends EntityArrow implements IRiftProjectile, IProjectile {
    private EntityPlayer rider;

    public Mudball(World worldIn) {
        super(worldIn);
    }

    public Mudball(World world, double x, double y, double z) {
        this(world);
        this.setDamage(4D);
        this.setPosition(x, y, z);
    }

    public Mudball(World world, EntityLivingBase shooter) {
        this(world, shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.1, shooter.posZ);
        this.shootingEntity = shooter;
    }

    public Mudball(World world, EntityLivingBase shooter, EntityPlayer rider) {
        this(world, rider.posX, rider.posY + rider.getEyeHeight() - 0.1, rider.posZ);
        this.shootingEntity = shooter;
        this.rider = rider;
    }


    protected void onHit(RayTraceResult raytraceResultIn) {
        Entity entity = raytraceResultIn.entityHit;

        if (!this.world.isRemote) {
            if (entity != null) {
                if (entity instanceof RiftCreaturePart) {
                    RiftCreaturePart part = (RiftCreaturePart) entity;
                    RiftCreature parent = part.getParent();

                    if (parent != this.shootingEntity) {
                        float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                        int i = MathHelper.ceil((double) f * this.getDamage());

                        if (this.getIsCritical()) i += this.rand.nextInt(i / 2 + 2);

                        DamageSource damagesource;

                        if (this.shootingEntity == null) damagesource = DamageSource.causeArrowDamage(this, this);
                        else damagesource = DamageSource.causeArrowDamage(this, this.shootingEntity);

                        if (entity.attackEntityFrom(damagesource, (float) i)) {
                            parent.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 10 * 20));
                            this.playSound(SoundEvents.BLOCK_SLIME_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                            this.setDead();
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
                else if (entity != this.rider) {
                    float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                    int i = MathHelper.ceil((double) f * this.getDamage());

                    if (this.getIsCritical()) i += this.rand.nextInt(i / 2 + 2);

                    DamageSource damagesource;

                    if (this.shootingEntity == null) damagesource = DamageSource.causeArrowDamage(this, this);
                    else damagesource = DamageSource.causeArrowDamage(this, this.shootingEntity);

                    if (entity.attackEntityFrom(damagesource, (float) i)) {
                        if (entity instanceof EntityLivingBase) {
                            EntityLivingBase entitylivingbase = (EntityLivingBase) entity;
                            entitylivingbase.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 10 * 20));
                            this.arrowHit(entitylivingbase);
                        }
                        this.playSound(SoundEvents.BLOCK_SLIME_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                        this.setDead();
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
        }
        else super.onHit(raytraceResultIn);
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
    protected ItemStack getArrowStack() {
        return null;
    }

    @Override
    public ItemStack getItemToRender() {
        return new ItemStack(RiftProjectileAnimatorRegistry.MUDBALL);
    }
}
