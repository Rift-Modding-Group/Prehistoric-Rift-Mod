package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HorizontalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.server.effect.RiftEffects;
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
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceTintingComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState.ParticleInitialSpeedComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime.ParticleLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleMotion.ParticleMotionDynamicComponent;
import anightdazingzoroark.riftlib.sounds.RiftLibSoundHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class VoltTackleBuilder extends CreatureMoveBuilder {
    public VoltTackleBuilder() {
        this.setMakesContact()
                .setElement(Element.ELECTRIC)
                .setMoveType(MoveType.PHYSICAL)
                .setRequireFindTargetToUse()
                .setStaminaCost(0.05f)
                .setBasePower(70)
                .setRiddenAimingType(RiddenAimingType.RANGED)
                .setRiddenPitchBounds(0f, 0f)
                .setMoveChargeupBuilder(new CreatureMoveChargeupBuilder()
                        .setChargeUpThenRelease()
                        .setCanRotateWhileCharging()
                        .setMaxChargeUp(100)
                        .setCooldownMultiplier(4D)
                        .setBasePowerMultiplier((creature, basePower, chargeUp) -> {
                            return MathUtil.slopeResult(chargeUp, false, 0, 100, 1D, 2D);
                        })
                        .setWindupEffect((creature, charge) -> {
                            RiftLibSoundHelper.playLoopingSound(
                                    creature.getAnimationData().getHolder(),
                                    RiftSounds.getSound("generic_move.electric_move_loop"),
                                    SoundCategory.NEUTRAL
                            );
                            RiftLibParticleHelper.createParticle(
                                    new ParticleBuilder().setIdentifier("prift:volt_tackle_prepare")
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
                                                    .setTextureWidth(128).setTextureHeight(128).setFlipbook(true)
                                                    .setUv(0D, 72D).setUvSize(8D, 8D).setUvStepSize(8D, 0D)
                                                    .setFramesPerSecond(20f).setMaxFrame(7D).setLoop(true))
                                            .addParticleComponent(new AppearanceTintingComponent.Builder()
                                                    .setRed(1D).setGreen(0.88235D).setBlue(0D).setAlpha(1D)),
                                    "centerPoint",
                                    (Entity & IAnimatable<?>) creature.getAnimationData().getHolder()
                            );
                        })
                        .setWindupEndEffect((creature, charge) -> {
                            RiftLibSoundHelper.stopLoopingSound(
                                    creature.getAnimationData().getHolder(),
                                    RiftSounds.getSound("generic_move.electric_move_loop"),
                                    SoundCategory.NEUTRAL
                            );
                            RiftLibParticleHelper.killParticle(
                                    (Entity & IAnimatable<?>) creature.getAnimationData().getHolder(),
                                    "prift:volt_tackle_prepare", "centerPoint"
                            );

                            //rush effects
                            creature.setRecoilDamage(damage -> damage);
                            int maximumRushTicks = (int) Math.ceil(MathUtil.slopeResult(charge, true, 0, 100, 0, 20));
                            creature.prepareRush(8D, maximumRushTicks, false, impactCreature -> {
                                Vec3d impactPosition = impactCreature.getLocatorWorldPos("bodyCenter");
                                RiftLibParticleHelper.createParticle(
                                        impactCreature.getEntityWorld(),
                                        new ParticleBuilder().setIdentifier("prift:volt_tackle_impact")
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
                                                        .setTextureWidth(128).setTextureHeight(128).setFlipbook(true)
                                                        .setUv(0D, 72D).setUvSize(8D, 8D).setUvStepSize(8D, 0D)
                                                        .setFramesPerSecond(20f).setMaxFrame(7D).setLoop(true))
                                                .addParticleComponent(new AppearanceTintingComponent.Builder()
                                                        .setRed(1D).setGreen(0.88235D).setBlue(0D).setAlpha(1D)),
                                        impactPosition.x, impactPosition.y, impactPosition.z
                                );
                            });

                            Vec3d creaturePos = creature.getPositionVector();
                            creature.getEntityWorld().playSound(
                                    null, creaturePos.x, creaturePos.y, creaturePos.z,
                                    RiftSounds.getSound("generic_move.electric_release"),
                                    SoundCategory.NEUTRAL, 1f, 1f
                            );

                            double effectWidth = creature.getSize()[0];
                            RiftLibParticleHelper.createParticle(
                                    new ParticleBuilder().setIdentifier("prift:volt_tackle")
                                            .setTexture(new ResourceLocation("textures/particle/particles.png"))
                                            .setMaterial(ParticleMaterial.ALPHA)
                                            .addEmitterComponent(new EmitterSteadyComponent.Builder()
                                                    .setSpawnRate(48D).setMaxParticleCount(100D))
                                            .addEmitterComponent(new EmitterLifetimeLoopingComponent.Builder().setActiveTime(1D))
                                            .addEmitterComponent(new EmitterShapeBoxComponent.Builder().setDirection(0, 0, 1).setSurfaceOnly(true)
                                                    .setHalfDimensions(effectWidth, effectWidth, effectWidth)
                                            )
                                            .addParticleComponent(new ParticleLifetimeExpressionComponent.Builder().setMaxLifetime(RawMolangValue.compileExpression("math.random(0.2, 0.4)")))
                                            .addParticleComponent(new ParticleInitialSpeedComponent.Builder().setInitialSpeed(5D))
                                            .addParticleComponent(new ParticleMotionDynamicComponent.Builder())
                                            .addParticleComponent(new AppearanceBillboardComponent.Builder()
                                                    .setSize(0.2D, 0.2D).setCameraMode(ParticleCameraMode.ROTATE_XYZ)
                                                    .setTextureWidth(128).setTextureHeight(128).setFlipbook(true)
                                                    .setUv(0D, 72D).setUvSize(8D, 8D).setUvStepSize(8D, 0D)
                                                    .setFramesPerSecond(20f).setMaxFrame(7D).setLoop(true))
                                            .addParticleComponent(new AppearanceTintingComponent.Builder()
                                                    .setRed(1D).setGreen(0.88235D).setBlue(0D).setAlpha(1D)),
                                    "bodyCenter",
                                    (Entity & IAnimatable<?>) creature.getAnimationData().getHolder()
                            );
                        })
                        .setOnHitEntityDuringRelease((creature, chargeUp, target) -> {
                            if (!(target instanceof EntityLivingBase targetLiving)) return;
                            targetLiving.addPotionEffect(new PotionEffect(RiftEffects.PARALYSIS, 300));
                        })
                )
                .setOnMoveHitEffect(creature -> {
                    Vec3d creaturePos = creature.getPositionVector();
                    creature.getEntityWorld().playSound(
                            null, creaturePos.x, creaturePos.y, creaturePos.z,
                            RiftSounds.getSound("generic_move.electric_release"),
                            SoundCategory.NEUTRAL, 1f, 1f
                    );
                })
                .setOnMoveEndEffect(creature -> {
                    RiftLibParticleHelper.killParticle(
                            (Entity & IAnimatable<?>) creature.getAnimationData().getHolder(),
                            "prift:volt_tackle", "bodyCenter"
                    );
                });
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("volt_tackle");
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
