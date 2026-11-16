package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HorizontalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.VerticalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleCameraMode;
import anightdazingzoroark.riftlib.particle.ParticleMaterial;
import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterSteadyComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapePointComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceBillboardComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceTintingComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState.ParticleInitialSpeedComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime.ParticleLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleMotion.ParticleMotionDynamicComponent;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import anightdazingzoroark.riftlib.ray.RiftLibRayHelper;
import anightdazingzoroark.riftlib.ray.rayShape.motion.RiftLibRayConeMotionShape;
import anightdazingzoroark.riftlib.sounds.RiftLibSoundHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

public class SludgeWaveBuilder extends CreatureMoveBuilder {
    public SludgeWaveBuilder() {
        this.setStaminaCost(0.08f)
                .setStaminaDrainPerSecond(0.0125f)
                .setBasePower(90)
                .setRequireFindTargetToUse()
                .setMoveType(CreatureMoveBuilder.MoveType.SPECIAL)
                .setElement(Element.POISON)
                .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureRayLook(creature, target, "straightCreatureRaySource"))
                .setWhileMoveUseEffect((creature, target) -> {
                    if (!creature.getAnimationData().getHolder().isBeingRidden() && (target == null || !target.isEntityAlive())) return;
                    CreatureLookUtil.captureRayLook(creature, target, "straightCreatureRaySource");
                })
                .setMoveChargeupBuilder(new CreatureMoveChargeupBuilder()
                        .setChargeType(CreatureMoveChargeupBuilder.ChargeType.CHARGE_UP_WHILE_USE)
                        .setCanRotate()
                        .setCanLookWhileUsing()
                        .setMaxChargeUp(100)
                        .setCooldownMultiplier(6D)
                        .setPrereleaseEndEffect(creature -> {
                            //create the particles
                            RiftLibParticleHelper.createParticle(
                                    new ParticleBuilder().setIdentifier("prift:sludge_wave_sludge")
                                            .setTexture(new ResourceLocation("textures/particle/particles.png"))
                                            .setMaterial(ParticleMaterial.ALPHA)
                                            .addEmitterComponent(new EmitterSteadyComponent.Builder()
                                                    .setSpawnRate(96D).setMaxParticleCount(220D))
                                            .addEmitterComponent(new EmitterLifetimeExpressionComponent.Builder()
                                                    .setActivationExpression(1D).setExpirationExpression(0D))
                                            .addEmitterComponent(new EmitterShapePointComponent.Builder().setDirection(
                                                    RawMolangValue.compileExpression("math.random(-0.35, 0.35)"),
                                                    RawMolangValue.compileExpression("math.random(-0.08, 0.08)"),
                                                    new Constant(-1D)))
                                            .addParticleComponent(new ParticleLifetimeExpressionComponent.Builder()
                                                    .setMaxLifetime("math.random(0.95, 1.15)"))
                                            .addParticleComponent(new AppearanceTintingComponent.Builder()
                                                    .setRed(RawMolangValue.compileExpression("variable.particle_random_1 * 0.5294"))
                                                    .setGreen(RawMolangValue.compileExpression("variable.particle_random_1 * 0.6392"))
                                                    .setBlue(RawMolangValue.compileExpression("variable.particle_random_1 * 0.3882"))
                                                    .setAlpha(1D)
                                            )
                                            .addParticleComponent(new ParticleInitialSpeedComponent.Builder().setInitialSpeed(14D))
                                            .addParticleComponent(new ParticleMotionDynamicComponent.Builder())
                                            .addParticleComponent(new AppearanceBillboardComponent.Builder()
                                                    .setSize(0.5D, 0.5D).setCameraMode(ParticleCameraMode.ROTATE_XYZ)
                                                    .setTextureWidth(128).setTextureHeight(128).setFlipbook(true)
                                                    .setUv(56D, 0D).setUvSize(8D, 8D).setFramesPerSecond(8)
                                                    .setMaxFrame(8).setStretchToLifetime(true).setLoop(false)
                                            ),
                                    "straightCreatureRaySource",
                                    (Entity & IAnimatable<?>) creature.getAnimationData().getHolder()
                            );

                            //create the ray
                            creature.createRay(
                                    new RiftLibRayBuilder("prift:sludge_wave")
                                            .setMotionOnly()
                                            .setMovementShape(() -> new RiftLibRayConeMotionShape(0.1D, 0.35D, 0.25D, 3, 12, false, false))
                                            .setMotionSpeed(1D)
                                            .setMaxMotionDistance(16D)
                                            .setHitHandler((rayCreator, rayOrigin, rayHitResult) -> {
                                                if (!(rayCreator instanceof ICreature source)) return;
                                                for (Entity hitEntity : rayHitResult.hitEntities()) {
                                                    if (!(hitEntity instanceof EntityLivingBase hitLiving)) continue;
                                                    source.attackEntityAsMob(hitEntity);
                                                    hitLiving.addPotionEffect(new PotionEffect(MobEffects.POISON, 300, 2));
                                                }
                                            }),
                                    "straightCreatureRaySource"
                            );

                            RiftLibSoundHelper.playLoopingSound(
                                    creature.getAnimationData().getHolder(),
                                    RiftSounds.getSound("sludge_wave.use_loop"),
                                    SoundCategory.NEUTRAL
                            );
                        })
                        .setReleaseEndEffect(creature -> {
                            RiftLibSoundHelper.stopLoopingSound(
                                    creature.getAnimationData().getHolder(),
                                    RiftSounds.getSound("sludge_wave.use_loop"),
                                    SoundCategory.NEUTRAL
                            );
                            RiftLibParticleHelper.killParticle(
                                    (Entity & IAnimatable<?>) creature.getAnimationData().getHolder(),
                                    "prift:sludge_wave_sludge", "straightCreatureRaySource"
                            );
                            RiftLibRayHelper.killRay(creature.getAnimationData().getHolder(), "prift:sludge_wave");
                        })
                )
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.RANGED)
                .setRiddenAimLocator("straightCreatureRaySource", 16D)
                .setRiddenPitchBounds(-75f, 35f)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.POINT);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("sludge_wave");
            this.setPriorityPredicate((creature, target) -> {
                        if (target != null && target.isEntityAlive() && (creature.atRageThreshold() || creature.isUnableToPathToTarget())) {
                            return 0;
                        }
                        return -1;
                    })
                    .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(6D, 16D))
                    .addDetectionRule(new VerticalDistanceFromUserDetectionRule(5D, 16D))
                    .setUseWhenFrustrated();
        }
    }
}
