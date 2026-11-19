package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HitboxDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleCameraMode;
import anightdazingzoroark.riftlib.particle.ParticleMaterial;
import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterInitialState.EmitterLocalSpaceComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterSteadyComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapeDiscComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapeSphereComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceBillboardComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceTintingComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState.ParticleInitialSpeedComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime.ParticleLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleMotion.ParticleMotionDynamicComponent;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import anightdazingzoroark.riftlib.ray.RiftLibRayHelper;
import anightdazingzoroark.riftlib.ray.rayShape.motion.RiftLibRayStraightMotionShape;
import anightdazingzoroark.riftlib.sounds.RiftLibSoundHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

public class SolarBeamBuilder extends CreatureMoveBuilder {
    public SolarBeamBuilder() {
        this.setElement(Element.GRASS)
                .setMoveType(CreatureMoveBuilder.MoveType.SPECIAL)
                .setRequireFindTargetToUse()
                .setPreventMovement()
                .setStaminaCost(0.08f)
                .setStaminaDrainPerSecond(0.0125f)
                .setBasePower(60)
                .setCanUse(creature -> creature.getEntityWorld().isDaytime())
                .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.initializeRayLook(creature, target, "straightCreatureRaySource"))
                .setOnMoveEndEffect(creature -> RiftLibParticleHelper.killParticle(
                        (Entity & IAnimatable<?>) creature.getAnimationData().getHolder(),
                        "prift:solar_beam_prepare", "straightCreatureRaySource"
                ))
                .setMoveChargeupBuilder(new CreatureMoveChargeupBuilder()
                        .setChargeType(CreatureMoveChargeupBuilder.ChargeType.CHARGE_UP_THEN_RELEASE)
                        .setMaxChargeUp(100)
                        .setReleaseDuration(100)
                        .setCooldownMultiplier(6D)
                        .setCanRotateWhileCharging()
                        .setWindupStartEffect(creature -> {
                            RiftLibSoundHelper.playLoopingSound(
                                    creature.getAnimationData().getHolder(),
                                    RiftSounds.getSound("solar_beam.charge_loop"),
                                    SoundCategory.NEUTRAL
                            );
                        })
                        .setWindupEffect((creature, charge) -> {
                            CreatureLookUtil.updateRayLook(creature, creature.getAttackTarget(), "straightCreatureRaySource");
                            if (charge > 0) return;

                            RiftLibParticleHelper.createParticle(
                                    new ParticleBuilder().setIdentifier("prift:solar_beam_prepare")
                                            .setTexture(new ResourceLocation("textures/entity/experience_orb.png"))
                                            .setMaterial(ParticleMaterial.ADD)
                                            .addEmitterComponent(new EmitterSteadyComponent.Builder()
                                                    .setSpawnRate(32D).setMaxParticleCount(24D))
                                            .addEmitterComponent(new EmitterLifetimeExpressionComponent.Builder()
                                                    .setActivationExpression(1D).setExpirationExpression(0D))
                                            .addEmitterComponent(new EmitterLocalSpaceComponent.Builder().setPosition(true))
                                            .addEmitterComponent(new EmitterShapeSphereComponent.Builder().setRadius(0.25D))
                                            .addParticleComponent(new ParticleLifetimeExpressionComponent.Builder().setMaxLifetime(0.35D))
                                            .addParticleComponent(new ParticleInitialSpeedComponent.Builder().setInitialSpeed(0D))
                                            .addParticleComponent(new AppearanceBillboardComponent.Builder()
                                                    .setSize(0.18D, 0.18D).setCameraMode(ParticleCameraMode.ROTATE_XYZ)
                                                    .setTextureWidth(64).setTextureHeight(64)
                                                    .setUv(0D, 0D).setUvSize(16D, 16D))
                                            .addParticleComponent(new AppearanceTintingComponent.Builder().setColor(0xFFF39A, false)),
                                    "straightCreatureRaySource",
                                    (Entity & IAnimatable<?>) creature.getAnimationData().getHolder()
                            );
                        })
                        .setWindupEndEffect((creature, charge) -> {
                            CreatureLookUtil.updateRayLook(creature, creature.getAttackTarget(), "straightCreatureRaySource");
                            RiftLibParticleHelper.killParticle(
                                    (Entity & IAnimatable<?>) creature.getAnimationData().getHolder(),
                                    "prift:solar_beam_prepare", "straightCreatureRaySource"
                            );

                            RiftLibSoundHelper.stopLoopingSound(
                                    creature.getAnimationData().getHolder(),
                                    RiftSounds.getSound("solar_beam.charge_loop"),
                                    SoundCategory.NEUTRAL
                            );
                        })
                        .setBasePowerMultiplier((creature, basePower, chargeUp) -> {
                            return MathUtil.slopeResult(chargeUp, true, 0, 100, 1D, 3D);
                        })
                        .setPrereleaseEndEffect(creature -> {
                            //create the particles
                            RiftLibParticleHelper.createParticle(
                                    new ParticleBuilder().setIdentifier("prift:solar_beam_ray")
                                            .setTexture(new ResourceLocation("textures/entity/experience_orb.png"))
                                            .setMaterial(ParticleMaterial.ADD)
                                            .addEmitterComponent(new EmitterSteadyComponent.Builder()
                                                    .setSpawnRate(96D).setMaxParticleCount(192D))
                                            .addEmitterComponent(new EmitterLifetimeExpressionComponent.Builder()
                                                    .setActivationExpression(1D).setExpirationExpression(0D))
                                            .addEmitterComponent(new EmitterShapeDiscComponent.Builder().setRadius(0.75D).setPlaneNormal(0D, 0D, 1D).setDirection(0D, 0D, -1D))
                                            .addParticleComponent(new ParticleLifetimeExpressionComponent.Builder().setMaxLifetime(0.8D))
                                            .addParticleComponent(new ParticleInitialSpeedComponent.Builder().setInitialSpeed(20D))
                                            .addParticleComponent(new ParticleMotionDynamicComponent.Builder())
                                            .addParticleComponent(new AppearanceBillboardComponent.Builder()
                                                    .setSize(0.18D, 0.18D).setCameraMode(ParticleCameraMode.ROTATE_XYZ)
                                                    .setTextureWidth(64).setTextureHeight(64)
                                                    .setUv(0D, 0D).setUvSize(16D, 16D))
                                            .addParticleComponent(new AppearanceTintingComponent.Builder().setColor(0xFFF39A, false)),
                                    "straightCreatureRaySource",
                                    (Entity & IAnimatable<?>) creature.getAnimationData().getHolder()
                            );

                            //create the ray
                            creature.createRay(
                                    new RiftLibRayBuilder("prift:solar_beam")
                                            .setMotionOnly()
                                            .setMovementShape(() -> new RiftLibRayStraightMotionShape(0.1D, 0.5D))
                                            .setMotionSpeed(1D)
                                            .setMaxMotionDistance(16D)
                                            .setHitHandler((rayCreator, rayOrigin, rayHitResult) -> {
                                                for (Entity hitEntity : rayHitResult.hitEntities()) {
                                                    if (creature.isRelatedToEntity(hitEntity)) continue;
                                                    if (hitEntity instanceof EntityLivingBase) creature.attackEntityAsMob(hitEntity);
                                                }
                                            }),
                                    "straightCreatureRaySource"
                            );

                            RiftLibSoundHelper.playLoopingSound(
                                    creature.getAnimationData().getHolder(),
                                    RiftSounds.getSound("solar_beam.use_loop"),
                                    SoundCategory.NEUTRAL
                            );
                        })
                        .setReleaseEndEffect(creature -> {
                            RiftLibSoundHelper.stopLoopingSound(
                                    creature.getAnimationData().getHolder(),
                                    RiftSounds.getSound("solar_beam.use_loop"),
                                    SoundCategory.NEUTRAL
                            );
                            RiftLibParticleHelper.killParticle(
                                    (Entity & IAnimatable<?>) creature.getAnimationData().getHolder(),
                                    "prift:solar_beam_ray", "straightCreatureRaySource"
                            );
                            RiftLibRayHelper.killRay(creature.getAnimationData().getHolder(), "prift:solar_beam");
                        })
                )
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.RANGED)
                .setRiddenAimLocator("straightCreatureRaySource", 16D)
                .setRiddenPitchBounds(-75f, 35f)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.POINT);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("solar_beam", MoveCategory.RANGED);
            this.setPriorityPredicate((creature, target) -> target != null && target.isEntityAlive())
                    .addDetectionRule(new HitboxDistanceFromUserDetectionRule(4D));
        }
    }
}
