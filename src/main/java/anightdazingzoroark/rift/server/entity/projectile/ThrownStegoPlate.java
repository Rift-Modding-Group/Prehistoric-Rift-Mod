package anightdazingzoroark.rift.server.entity.projectile;

import anightdazingzoroark.rift.server.entity.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class ThrownStegoPlate extends EntityArrow implements IAnimatable {
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(RiftCreature.class, DataSerializers.VARINT);
    public AnimationFactory factory = new AnimationFactory(this);

    public ThrownStegoPlate(World worldIn) {
        super(worldIn);
    }

    public ThrownStegoPlate(World world, double x, double y, double z) {
        this(world);
        this.setDamage(4D);
        this.setPosition(x, y, z);
    }

    public ThrownStegoPlate(World world, EntityLivingBase shooter) {
        this(world, shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.1, shooter.posZ);
        this.shootingEntity = shooter;
        this.setDamage(4D);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANT, 0);
    }

    protected void onHit(RayTraceResult raytraceResultIn) {
        Entity entity = raytraceResultIn.entityHit;

        if (entity != null) {
            float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
            int i = MathHelper.ceil((double) f * this.getDamage());

            if (this.getIsCritical()) i += this.rand.nextInt(i / 2 + 2);

            DamageSource damagesource;

            if (this.shootingEntity == null) damagesource = DamageSource.causeArrowDamage(this, this);
            else damagesource = DamageSource.causeArrowDamage(this, this.shootingEntity);

            if (entity.attackEntityFrom(damagesource, (float) i)) {
                if (entity instanceof EntityLivingBase) {
                    EntityLivingBase entitylivingbase = (EntityLivingBase) entity;
//                    if (!this.world.isRemote) entitylivingbase.setArrowCountInEntity(entitylivingbase.getArrowCountInEntity() + 1);
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
                if (!this.world.isRemote && this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ < 0.0010000000474974513D) {
                    this.setDead();
                }
            }
        }
        else {
            super.onHit(raytraceResultIn);
        }
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

    @Override
    public void registerControllers(AnimationData data) {}

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
