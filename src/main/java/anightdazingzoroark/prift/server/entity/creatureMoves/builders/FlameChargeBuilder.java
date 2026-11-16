package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HorizontalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleCameraMode;
import anightdazingzoroark.riftlib.particle.ParticleMaterial;
import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeLoopingComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeOnceComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterInstantComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterSteadyComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapeBoxComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapeDiscComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapeSphereComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceBillboardComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState.ParticleInitialSpeedComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime.ParticleLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleMotion.ParticleMotionDynamicComponent;
import anightdazingzoroark.riftlib.sounds.RiftLibSoundHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class FlameChargeBuilder extends CreatureMoveBuilder {
    public FlameChargeBuilder() {
        this.setMakesContact()
                .setElement(Element.FIRE)
                .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                .setRequireFindTargetToUse()
                .setStaminaCost(0.05f)
                .setBasePower(70)
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.CHARGE)
                .setRiddenPitchBounds(0f, 0f)
                .setMoveChargeupBuilder(new CreatureMoveChargeupBuilder()
                        .setChargeType(CreatureMoveChargeupBuilder.ChargeType.CHARGE_UP_THEN_RELEASE)
                        .setCanRotateWhileCharging()
                        .setMaxChargeUp(100)
                        .setCooldownMultiplier(4D)
                        .setBasePowerMultiplier((creature, basePower, chargeUp) -> {
                            return MathUtil.slopeResult(chargeUp, false, 0, 100, 1D, 2D);
                        })
                        .setWindupEffect((creature, charge) -> {
                            RiftLibSoundHelper.playLoopingSound(
                                    creature.getAnimationData().getHolder(),
                                    RiftSounds.getSound("flamethrower.breathe_loop"),
                                    SoundCategory.NEUTRAL
                            );
                            RiftLibParticleHelper.createParticle(
                                    new ParticleBuilder().setIdentifier("prift:flame_charge_prepare")
                                            .setTexture(new ResourceLocation("textures/particle/particles.png"))
                                            .setMaterial(ParticleMaterial.ALPHA)
                                            .addEmitterComponent(new EmitterSteadyComponent.Builder()
                                                    .setSpawnRate(48D).setMaxParticleCount(100D))
                                            .addEmitterComponent(new EmitterLifetimeLoopingComponent.Builder().setActiveTime(1D))
                                            .addEmitterComponent(new EmitterShapeDiscComponent.Builder().setRadius(creature.getSize()[0] + 0.5D)
                                                    .setDirection(0, 1D, 0)
                                            )
                                            .addParticleComponent(new ParticleLifetimeExpressionComponent.Builder().setMaxLifetime(RawMolangValue.compileExpression("math.random(0.2, 0.4)")))
                                            .addParticleComponent(new ParticleInitialSpeedComponent.Builder().setInitialSpeed(5D))
                                            .addParticleComponent(new ParticleMotionDynamicComponent.Builder())
                                            .addParticleComponent(new AppearanceBillboardComponent.Builder()
                                                    .setSize(0.2D, 0.2D).setCameraMode(ParticleCameraMode.ROTATE_XYZ)
                                                    .setTextureWidth(128).setTextureHeight(128)
                                                    .setUv(0D, 24D).setUvSize(8D, 8D)),
                                    "centerPoint",
                                    (Entity & IAnimatable<?>) creature.getAnimationData().getHolder()
                            );
                        })
                        .setWindupEndEffect((creature, charge) -> {
                            RiftLibSoundHelper.stopLoopingSound(
                                    creature.getAnimationData().getHolder(),
                                    RiftSounds.getSound("flamethrower.breathe_loop"),
                                    SoundCategory.NEUTRAL
                            );
                            RiftLibParticleHelper.killParticle(
                                    (Entity & IAnimatable<?>) creature.getAnimationData().getHolder(),
                                    "prift:flame_charge_prepare", "centerPoint"
                            );

                            //rush effects
                            creature.setRecoilDamage(damage -> damage * 0.25D);
                            int maximumRushTicks = (int) Math.ceil(MathUtil.slopeResult(charge, true, 0, 100, 0, 100));
                            creature.prepareRush(4D, maximumRushTicks, false, impactCreature -> {
                                Vec3d impactPosition = impactCreature.getLocatorWorldPos("bodyCenter");
                                RiftLibParticleHelper.createParticle(
                                        impactCreature.getEntityWorld(),
                                        new ParticleBuilder().setIdentifier("prift:flame_charge_impact")
                                                .setTexture(new ResourceLocation("textures/particle/particles.png"))
                                                .setMaterial(ParticleMaterial.ALPHA)
                                                .addEmitterComponent(new EmitterInstantComponent.Builder().setParticleCount(64D))
                                                .addEmitterComponent(new EmitterLifetimeOnceComponent.Builder().setActiveTime(0.1D))
                                                .addEmitterComponent(new EmitterShapeSphereComponent.Builder()
                                                        .setRadius(impactCreature.getSize()[0] * 0.5D)
                                                        .setSurfaceOnly(true).setDirection("outwards"))
                                                .addParticleComponent(new ParticleLifetimeExpressionComponent.Builder()
                                                        .setMaxLifetime(RawMolangValue.compileExpression("math.random(0.3, 0.6)")))
                                                .addParticleComponent(new ParticleInitialSpeedComponent.Builder().setInitialSpeed(6D))
                                                .addParticleComponent(new ParticleMotionDynamicComponent.Builder())
                                                .addParticleComponent(new AppearanceBillboardComponent.Builder()
                                                        .setSize(0.2D, 0.2D).setCameraMode(ParticleCameraMode.ROTATE_XYZ)
                                                        .setTextureWidth(128).setTextureHeight(128)
                                                        .setUv(0D, 24D).setUvSize(8D, 8D)),
                                        impactPosition.x, impactPosition.y, impactPosition.z
                                );
                            });

                            Vec3d creaturePos = creature.getPositionVector();
                            creature.getEntityWorld().playSound(
                                    null, creaturePos.x, creaturePos.y, creaturePos.z,
                                    RiftSounds.getSound("flamethrower.breathe_start"),
                                    SoundCategory.NEUTRAL, 1f, 1f
                            );

                            double effectWidth = creature.getSize()[0];
                            RiftLibParticleHelper.createParticle(
                                    new ParticleBuilder().setIdentifier("prift:flame_charge")
                                            .setTexture(new ResourceLocation("textures/particle/particles.png"))
                                            .setMaterial(ParticleMaterial.ALPHA)
                                            .addEmitterComponent(new EmitterSteadyComponent.Builder()
                                                    .setSpawnRate(80D).setMaxParticleCount(240D))
                                            .addEmitterComponent(new EmitterLifetimeLoopingComponent.Builder().setActiveTime(1D))
                                            .addEmitterComponent(new EmitterShapeBoxComponent.Builder().setDirection(0, 0, 1).setSurfaceOnly(true)
                                                    .setHalfDimensions(effectWidth, effectWidth, effectWidth)
                                            )
                                            .addParticleComponent(new ParticleLifetimeExpressionComponent.Builder().setMaxLifetime(RawMolangValue.compileExpression("math.random(0.2, 0.4)")))
                                            .addParticleComponent(new ParticleInitialSpeedComponent.Builder().setInitialSpeed(5D))
                                            .addParticleComponent(new ParticleMotionDynamicComponent.Builder())
                                            .addParticleComponent(new AppearanceBillboardComponent.Builder()
                                                    .setSize(0.2D, 0.2D).setCameraMode(ParticleCameraMode.ROTATE_XYZ)
                                                    .setTextureWidth(128).setTextureHeight(128)
                                                    .setUv(0D, 24D).setUvSize(8D, 8D)),
                                    "bodyCenter",
                                    (Entity & IAnimatable<?>) creature.getAnimationData().getHolder()
                            );
                        })
                        .setOnHitEntityDuringRelease((creature, chargeUp, target) -> {
                            target.setFire(15);
                        })
                )
                .setOnMoveHitEffect(creature -> {
                    Vec3d creaturePos = creature.getPositionVector();
                    creature.getEntityWorld().playSound(
                            null, creaturePos.x, creaturePos.y, creaturePos.z,
                            RiftSounds.getSound("generic_move.fire_release"),
                            SoundCategory.NEUTRAL, 1f, 1f
                    );
                })
                .setOnMoveEndEffect(creature -> {
                    RiftLibParticleHelper.killParticle(
                            (Entity & IAnimatable<?>) creature.getAnimationData().getHolder(),
                            "prift:flame_charge", "bodyCenter"
                    );
                });
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("flame_charge");
            this.setPriorityPredicate((creature, target) -> {
                        if (target != null && target.isEntityAlive() && creature.isOnGround()
                                && !creature.bodyTouchingLiquid() && creature.hasStraightWalkingPathTo(target)
                                && creature.horizontalDistanceFromEntity(target) >= 6D
                                && creature.horizontalDistanceFromEntity(target) <= 16D
                        ) {
                            return 4;
                        }
                        return -1;
                    })
                    .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(6D, 16D))
                    .setDontPathToTarget();
        }
    }
}
