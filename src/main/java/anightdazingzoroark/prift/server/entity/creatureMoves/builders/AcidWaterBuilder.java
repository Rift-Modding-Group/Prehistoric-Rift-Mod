package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HitboxDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.api.projectile.ProjectileTrail;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.prift.server.effect.RiftEffects;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleCameraMode;
import anightdazingzoroark.riftlib.particle.ParticleMaterial;
import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterInstantComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapeSphereComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceBillboardComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceTintingComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState.ParticleInitialSpeedComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime.ParticleLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleMotion.ParticleMotionDynamicComponent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class AcidWaterBuilder extends CreatureMoveBuilder {
    public AcidWaterBuilder() {
        this.setStaminaCost(0.08f)
                .setBasePower(30)
                .setRequireFindTargetToUse()
                .setElement(Element.WATER)
                .setMoveType(CreatureMoveBuilder.MoveType.SPECIAL)
                .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureHeadAttackLook(creature))
                .setOnMoveHitEffect(creature -> {
                    ProjectileBuilder acidWaterProjectile = new ProjectileBuilder().setName("acid_water")
                            .setProjectileModel(ProjectileBuilder.ProjectileModel.CUBE)
                            .setScale(2f)
                            .setProjectileTrail(ProjectileTrail.makeBuilderForParticleTrail()
                                    .addParticleComponent(new AppearanceTintingComponent.Builder().setColor(0x87A363, false))
                                    .addParticleComponent(ProjectileTrail.DUST_APPEARANCE)
                            )
                            .setImpactSoundEvent(RiftSounds.getSound("generic_water.hit"))
                            .setOnImpactFromCreatureEffect((iCreature, projectile, hitEntity, hitPos) -> {
                                hitEntity.addPotionEffect(new PotionEffect(RiftEffects.DEFENSE_DOWN, 400, 2));

                                //make particle
                                RiftLibParticleHelper.createParticle(
                                        projectile.getEntityWorld(),
                                        new ParticleBuilder().setIdentifier("prift:acid_water_impact")
                                                .setTexture(new ResourceLocation("textures/particle/particles.png"))
                                                .setMaterial(ParticleMaterial.ALPHA)
                                                .addEmitterComponent(new EmitterInstantComponent.Builder().setParticleCount(48))
                                                .addEmitterComponent(new EmitterLifetimeExpressionComponent.Builder())
                                                .addEmitterComponent(new EmitterShapeSphereComponent.Builder().setRadius(0.15D).setDirection("outwards"))
                                                .addParticleComponent(new ParticleLifetimeExpressionComponent.Builder().setMaxLifetime(1D))
                                                .addParticleComponent(new ParticleInitialSpeedComponent.Builder().setInitialSpeed(4D))
                                                .addParticleComponent(new ParticleMotionDynamicComponent.Builder())
                                                .addParticleComponent(new AppearanceBillboardComponent.Builder()
                                                        .setSize(0.2D, 0.2D).setCameraMode(ParticleCameraMode.ROTATE_XYZ)
                                                        .setTextureWidth(256).setTextureHeight(256)
                                                        .setFlipbook(true).setUv(112, 0).setUvSize(16, 16).setUvStepSize(-16, 0)
                                                        .setFramesPerSecond(10).setMaxFrame(8)
                                                )
                                                .addParticleComponent(new AppearanceTintingComponent.Builder().setColor(0x87A363, false)),
                                        hitPos.x, hitPos.y, hitPos.z
                                );
                            });

                    //when ridden, shoot forward
                    if (creature.getControllingPassenger() != null) {
                        Vec3d creaturePos = creature.getPositionVector();
                        creature.getEntityWorld().playSound(
                                null, creaturePos.x, creaturePos.y, creaturePos.z,
                                RiftSounds.getSound("generic_water.use"),
                                SoundCategory.NEUTRAL, 1f, 1f
                        );
                        creature.launchProjectile(acidWaterProjectile, 3f, 0f);
                    }
                    //otherwise, hit target
                    else {
                        EntityLivingBase target = creature.getAttackTarget();
                        if (target == null || !target.isEntityAlive()) return;

                        Vec3d creaturePos = creature.getPositionVector();
                        creature.getEntityWorld().playSound(
                                null, creaturePos.x, creaturePos.y, creaturePos.z,
                                RiftSounds.getSound("generic_water.use"),
                                SoundCategory.NEUTRAL, 1.5f, 1f
                        );
                        creature.launchProjectile(acidWaterProjectile, target, 3f, 0f);
                    }
                })
                .setUseLookBasedFacing()
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.RANGED)
                .setRiddenPitchBounds(-75f, 35f)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.POINT)
                .setCooldown(600);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("acid_water", MoveCategory.RANGED);
            this.setPriorityPredicate((creature, target) -> target != null && target.isEntityAlive())
                    .addDetectionRule(new HitboxDistanceFromUserDetectionRule(4D))
                    .setDontPathToTarget();
        }
    }
}
