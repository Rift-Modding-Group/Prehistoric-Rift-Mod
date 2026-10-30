package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleCameraMode;
import anightdazingzoroark.riftlib.particle.ParticleMaterial;
import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeOnceComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterInstantComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapePointComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceBillboardComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState.ParticleInitialSpeedComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime.ParticleLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleMotion.ParticleMotionDynamicComponent;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class FlameSpitBuilder extends CreatureMoveBuilder {
    public FlameSpitBuilder() {
        this.setStaminaCost(0.15f)
                .setBasePower(40)
                .setRequireFindTargetToUse()
                .setElement(Element.FIRE)
                .setMoveType(CreatureMoveBuilder.MoveType.SPECIAL)
                .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureHeadAttackLook(creature, "flame_spit"))
                .setOnMoveHitEffect(creature -> {
                    //particles
                    RiftLibParticleHelper.createParticle(
                            new ParticleBuilder().setIdentifier("prift:flame_spit")
                                    .setTexture(new ResourceLocation("textures/particle/particles.png"))
                                    .setMaterial(ParticleMaterial.ALPHA)
                                    .addEmitterComponent(new EmitterInstantComponent.Builder().setParticleCount(48D))
                                    .addEmitterComponent(new EmitterLifetimeOnceComponent.Builder().setActiveTime(1D))
                                    .addEmitterComponent(new EmitterShapePointComponent.Builder().setDirection(
                                            RawMolangValue.compileExpression("math.random(-0.15, 0.15)"),
                                            RawMolangValue.compileExpression("math.random(-0.15, 0.15)"),
                                            new Constant(-1D)))
                                    .addParticleComponent(new ParticleLifetimeExpressionComponent.Builder()
                                            .setMaxLifetime("math.random(0.95, 1.15)"))
                                    .addParticleComponent(new ParticleInitialSpeedComponent.Builder().setInitialSpeed(14D))
                                    .addParticleComponent(new ParticleMotionDynamicComponent.Builder())
                                    .addParticleComponent(new AppearanceBillboardComponent.Builder()
                                            .setSize(0.2D, 0.2D).setCameraMode(ParticleCameraMode.ROTATE_XYZ)
                                            .setTextureWidth(128).setTextureHeight(128)
                                            .setUv(0D, 24D).setUvSize(8D, 8D)),
                            "flamethrowerAimLocator",
                            (Entity & IAnimatable<?>) creature.getAnimationData().getHolder()
                    );

                    //flame spit hitbox
                    creature.createOffenseHitboxesByTag("flame_spit", 4, entity -> entity.setFire(100));
                })
                .setUseLookBasedFacing()
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE)
                .setCooldown(300);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("flame_spit");
            this.setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                    .addDetectionRule(new AnimatedHitboxDetectionRule("flame_spit", creature -> CreatureLookUtil.captureHeadAttackLook(creature, "flame_spit")))
                    .setUseBlockBreak();
        }
    }
}
