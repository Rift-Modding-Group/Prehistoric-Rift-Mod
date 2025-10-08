package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.riftlib.core.controller.AnimationController;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class RiftCreatureProjectileBuilder {
    //common to all
    public final RiftCreatureProjectile.Enum projectileEnum;

    //for damage to deal
    private Function<RiftCreatureProjectileEntity, Double> damageCalculator;

    //for sound on impact
    private SoundEvent impactSoundEvent;

    //for projectile animation
    //for now, its always assumed that projectiles always have 1 animation that always loops
    private boolean hasAnimation;
    private Function<RiftCreatureProjectileEntity, AnimationController> animationController;

    //for if the user has variants so that the texture changes based on the launchers variant
    private boolean hasVariants;

    //if true, no model
    private boolean hasNoModel;

    //if true, model is gonna be flat
    private boolean hasFlatModel;

    //if true, projectile cannot rotate vertically
    private boolean noVerticalRotate;

    //if true, projectile will self-destruct
    private boolean selfDestruct;

    //for on hit effect
    private BiConsumer<RiftCreatureProjectileEntity, EntityLivingBase> onHitEffect;

    //for delayed effect on impact stuff
    private boolean hasDelayedEffectOnImpact;
    private int delayedEffectOnImpactCountdown;
    private BiConsumer<RiftCreatureProjectileEntity, EntityLivingBase> delayedEffectOnImpact;

    //for power related stuff
    private boolean hasPower;
    private float[] powerParams; //array w 2 items, min and max power

    public RiftCreatureProjectileBuilder(RiftCreatureProjectile.Enum projectileEnum) {
        this.projectileEnum = projectileEnum;
    }

    public RiftCreatureProjectileBuilder setDamageCalculator(Function<RiftCreatureProjectileEntity, Double> damageCalculator) {
        this.damageCalculator = damageCalculator;
        return this;
    }

    public Function<RiftCreatureProjectileEntity, Double> getDamageCalculator() {
        return this.damageCalculator;
    }

    public RiftCreatureProjectileBuilder setImpactSoundEvent(SoundEvent impactSoundEvent) {
        this.impactSoundEvent = impactSoundEvent;
        return this;
    }

    public SoundEvent getImpactSoundEvent() {
        return this.impactSoundEvent;
    }

    public RiftCreatureProjectileBuilder setAnimation(Function<RiftCreatureProjectileEntity, AnimationController> animationController) {
        this.hasAnimation = true;
        this.animationController = animationController;
        return this;
    }

    public boolean getHasAnimation() {
        return this.hasAnimation;
    }

    public Function<RiftCreatureProjectileEntity, AnimationController> getAnimationController() {
        return this.animationController;
    }

    public RiftCreatureProjectileBuilder setHasVariants() {
        this.hasVariants = true;
        return this;
    }

    public boolean getHasVariants() {
        return this.hasVariants;
    }

    public RiftCreatureProjectileBuilder setHasNoModel() {
        this.hasNoModel = true;
        return this;
    }

    public boolean getHasNoModel() {
        return this.hasNoModel;
    }

    public RiftCreatureProjectileBuilder setHasFlatModel() {
        this.hasFlatModel = true;
        return this;
    }

    public boolean getHasFlatModel() {
        return this.hasFlatModel;
    }

    public RiftCreatureProjectileBuilder setNoVerticalRotation() {
        this.noVerticalRotate = true;
        return this;
    }

    public boolean getNoVerticalRotation() {
        return this.noVerticalRotate;
    }

    public RiftCreatureProjectileBuilder setSelfDestruct() {
        this.selfDestruct = true;
        return this;
    }

    public boolean getSelfDestruct() {
        return this.selfDestruct;
    }

    public RiftCreatureProjectileBuilder setOnHitEffect(BiConsumer<RiftCreatureProjectileEntity, EntityLivingBase> onHitEffect) {
        this.onHitEffect = onHitEffect;
        return this;
    }

    public BiConsumer<RiftCreatureProjectileEntity, EntityLivingBase> getOnHitEffect() {
        return this.onHitEffect;
    }

    public RiftCreatureProjectileBuilder setDelayedEffectOnImpact(int delayedEffectOnImpactCountdown, BiConsumer<RiftCreatureProjectileEntity, EntityLivingBase> delayedEffectOnImpact) {
        this.hasDelayedEffectOnImpact = true;
        this.delayedEffectOnImpactCountdown = delayedEffectOnImpactCountdown;
        this.delayedEffectOnImpact = delayedEffectOnImpact;
        return this;
    }

    public boolean getHasDelayedEffectOnImpact() {
        return this.hasDelayedEffectOnImpact;
    }

    public int getDelayedEffectOnImpactCountdown() {
        return this.delayedEffectOnImpactCountdown;
    }

    public BiConsumer<RiftCreatureProjectileEntity, EntityLivingBase> getDelayedEffectOnImpact() {
        return this.delayedEffectOnImpact;
    }

    public RiftCreatureProjectileBuilder setUsePower(float min, float max) {
        this.hasPower = true;
        this.powerParams = new float[]{min, max};
        return this;
    }

    public boolean getHasPower() {
        return this.hasPower;
    }

    public float[] getPowerParams() {
        return this.powerParams;
    }
}
