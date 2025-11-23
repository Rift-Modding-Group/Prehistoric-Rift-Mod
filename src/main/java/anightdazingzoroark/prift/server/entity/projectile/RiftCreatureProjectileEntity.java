package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.projectile.RiftLibProjectile;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

//this is for all projectiles launched by creatures
public class RiftCreatureProjectileEntity extends RiftLibProjectile {
    //builder gets saved here on the client too
    private static final DataParameter<String> PROJECTILE_BUILDER = EntityDataManager.createKey(RiftCreatureProjectileEntity.class, DataSerializers.STRING);

    //some projectiles have variants based on the creature that launched them, so here
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(RiftCreatureProjectileEntity.class, DataSerializers.VARINT);

    //some projectiles have a delayed explosion
    private boolean countingDown;
    private int countdown;

    //some projectiles have power related stuff attached, so they're dealt with here
    private float power;

    public RiftCreatureProjectileEntity(World worldIn) {
        super(worldIn);
    }

    public RiftCreatureProjectileEntity(RiftCreature shooter) {
        super(shooter.world, shooter);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(PROJECTILE_BUILDER, "");
        this.dataManager.register(VARIANT, 0);
    }

    public void onUpdate() {
        super.onUpdate();

        //count down until, well
        if (!this.world.isRemote) {
            if (this.getProjectileBuilder() != null && this.getProjectileBuilder().getHasDelayedEffectOnImpact() && this.countingDown) {
                this.countdown--;
                if (this.countdown <= 0) {
                    this.getProjectileBuilder().getDelayedEffectOnImpact().accept(this, null);
                }
            }
        }
    }

    public RiftCreatureProjectileBuilder getProjectileBuilder() {
        return RiftCreatureProjectile.getBuilderByName(this.dataManager.get(PROJECTILE_BUILDER));
    }

    public void setProjectileBuilder(RiftCreatureProjectileBuilder projectileBuilder) {
        this.dataManager.set(PROJECTILE_BUILDER, projectileBuilder.projectileName);
    }

    public boolean getHasVariants() {
        return this.getProjectileBuilder().getHasVariants();
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    public void setVariant(int value) {
        this.dataManager.set(VARIANT, value);
    }

    public void setCountdown(int value) {
        this.countdown = value;
    }

    public float getPower() {
        return this.power;
    }

    public void setPower(float value) {
        this.power = value;
    }

    public boolean hasFlatModel() {
        return this.getProjectileBuilder().getHasFlatModel();
    }

    public boolean hasNoModel() {
        return this.getProjectileBuilder().getHasNoModel();
    }

    @Override
    public void projectileEntityEffects(EntityLivingBase entityLivingBase) {
        if (this.getProjectileBuilder().getHasDelayedEffectOnImpact()) {
            this.countingDown = true;
        }
        if (this.getProjectileBuilder().getOnHitEffect() != null) {
            this.getProjectileBuilder().getOnHitEffect().accept(this, entityLivingBase);
        }
    }

    @Override
    public double getDamage() {
        if (this.getProjectileBuilder().getDamageCalculator() != null) {
            return this.getProjectileBuilder().getDamageCalculator().apply(this);
        }
        return 0;
    }

    @Override
    public boolean canSelfDestroyUponHit() {
        return this.getProjectileBuilder().getSelfDestruct();
    }

    @Override
    public boolean canRotateVertically() {
        return !this.getProjectileBuilder().getNoVerticalRotation();
    }

    public boolean getHasAnimation() {
        return this.getProjectileBuilder().getHasAnimation();
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        if (this.getProjectileBuilder().getHasAnimation()) {
            animationData.addAnimationController(this.getProjectileBuilder().getAnimationController().apply(this));
        }
    }

    @Override
    public SoundEvent getOnProjectileHitSound() {
        return this.getProjectileBuilder().getImpactSoundEvent();
    }
}
