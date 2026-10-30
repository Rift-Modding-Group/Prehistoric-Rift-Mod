package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HorizontalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.VerticalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

public class ToxicHazeBuilder extends CreatureMoveBuilder {
    public ToxicHazeBuilder() {
        this.setStaminaCost(0.08f)
                .setStaminaDrainPerSecond(0.0125f)
                .setRequireFindTargetToUse()
                .setMoveType(CreatureMoveBuilder.MoveType.STATUS)
                .setElement(Element.POISON)
                .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureRayLook(creature, target, "flamethrowerAimLocator"))
                .setWhileMoveUseEffect((creature, target) -> {
                    if (!creature.getAnimationData().getHolder().isBeingRidden() && (target == null || !target.isEntityAlive())) return;
                    CreatureLookUtil.captureRayLook(creature, target, "flamethrowerAimLocator");
                })
                .setMoveChargeupBuilder(new CreatureMoveChargeupBuilder()
                        .setChargeUpWhileUse(true)
                        .setCanLookWhileUsing(true)
                        .setMaxChargeUp(100)
                        .setCooldownMultiplier(1D)
                        .setPrereleaseEndEffect(creature -> {
                            //create the particles
                            RiftLibParticleHelper.createParticle(
                                    new ParticleBuilder().setIdentifier("prift:toxic_haze_smoke")
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
                                                    .setRed(RawMolangValue.compileExpression("variable.particle_random_1 * 0.5"))
                                                    .setBlue(RawMolangValue.compileExpression("variable.particle_random_1 * 0.5"))
                                                    .setGreen(RawMolangValue.compileExpression("variable.particle_random_1 * 0.5"))
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
                                    "flamethrowerAimLocator",
                                    (Entity & IAnimatable<?>) creature.getAnimationData().getHolder()
                            );

                            //create the ray
                            creature.createRay(
                                    new RiftLibRayBuilder("prift:toxic_haze")
                                            .setMotionOnly()
                                            .setMovementShape(() -> new RiftLibRayConeMotionShape(0.1D, 0.35D, 0.25D, 3, 12, false, false))
                                            .setMotionSpeed(1D)
                                            .setMaxMotionDistance(16D)
                                            .setHitHandler((rayCreator, rayOrigin, rayHitResult) -> {
                                                for (Entity hitEntity : rayHitResult.hitEntities()) {
                                                    if (!(hitEntity instanceof EntityLivingBase hitLiving))
                                                        continue;
                                                    hitLiving.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 200));
                                                    hitLiving.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 200, 2));
                                                }
                                            }),
                                    "flamethrowerAimLocator"
                            );
                        })
                        .setReleaseEndEffect(creature -> {
                            RiftLibParticleHelper.killParticle(
                                    (Entity & IAnimatable<?>) creature.getAnimationData().getHolder(),
                                    "prift:toxic_haze_smoke", "flamethrowerAimLocator"
                            );
                            RiftLibRayHelper.killRay(creature.getAnimationData().getHolder(), "prift:toxic_haze");
                        })
                )
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.RANGED)
                .setRiddenPitchBounds(-75f, 35f)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.POINT);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("toxic_haze");
            this.setPriorityPredicate((creature, target) -> {
                        if (target != null && target.isEntityAlive()) return 0;
                        return -1;
                    })
                    .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(6D, 16D))
                    .addDetectionRule(new VerticalDistanceFromUserDetectionRule(5D, 16D))
                    .setUseWhenFrustrated();
        }
    }
}
