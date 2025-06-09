package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreaturePart;
import anightdazingzoroark.prift.server.entity.interfaces.IRiftProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ThrownStegoPlate extends EntityArrow implements IRiftProjectile {
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(ThrownStegoPlate.class, DataSerializers.VARINT);
    private EntityPlayer rider;

    public ThrownStegoPlate(World worldIn) {
        super(worldIn);
    }

    public ThrownStegoPlate(World world, double x, double y, double z) {
        this(world);
        this.setDamage(4D);
        this.setPosition(x, y, z);
    }

    public ThrownStegoPlate(World world, RiftCreature shooter) {
        this(world, shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.1, shooter.posZ);
        this.shootingEntity = shooter;
    }

    public ThrownStegoPlate(World world, RiftCreature shooter, EntityPlayer rider) {
        super(world, shooter.riderPos().x, shooter.riderPos().y + rider.getEyeHeight() + rider.height, shooter.riderPos().z);
        this.shootingEntity = shooter;
        this.rider = rider;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANT, 0);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Variant", this.getVariant());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setVariant(compound.getInteger("Variant"));
    }

    public ItemStack getItemToRender() {
        ItemStack itemForm;
        switch (this.getVariant()) {
            default:
                itemForm = new ItemStack(RiftProjectileAnimatorRegistry.THROWN_STEGOSAURUS_PLATE_ONE);
                break;
            case 1:
                itemForm = new ItemStack(RiftProjectileAnimatorRegistry.THROWN_STEGOSAURUS_PLATE_TWO);
                break;
            case 2:
                itemForm = new ItemStack(RiftProjectileAnimatorRegistry.THROWN_STEGOSAURUS_PLATE_THREE);
                break;
            case 3:
                itemForm = new ItemStack(RiftProjectileAnimatorRegistry.THROWN_STEGOSAURUS_PLATE_FOUR);
                break;
        }
        return itemForm;
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

    public int getVariant() {
        return this.dataManager.get(VARIANT).intValue();
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }

    @Override
    protected ItemStack getArrowStack() {
        return null;
    }
}
