package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleCameraMode;
import anightdazingzoroark.riftlib.particle.ParticleMaterial;
import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeLoopingComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterSteadyComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapeSphereComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceBillboardComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState.ParticleInitialSpeedComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime.ParticleLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleMotion.ParticleMotionDynamicComponent;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class FireFangBuilder extends StandardMeleeBuilder {
    public FireFangBuilder() {
        this.setBasePower(50)
                .setElement(Element.FIRE)
                .setOnMoveBeginEffect((creature, target) -> {
                    CreatureLookUtil.captureHeadAttackLook(creature);

                    Vec3d creaturePos = creature.getPositionVector();
                    creature.getEntityWorld().playSound(
                            null, creaturePos.x, creaturePos.y, creaturePos.z,
                            RiftSounds.getSound("flamethrower.breathe_start"),
                            SoundCategory.NEUTRAL, 1f, 1f
                    );

                    RiftLibParticleHelper.createParticle(
                            new ParticleBuilder().setIdentifier("prift:fire_fang")
                                    .setTexture(new ResourceLocation("textures/particle/particles.png"))
                                    .setMaterial(ParticleMaterial.ALPHA)
                                    .addEmitterComponent(new EmitterSteadyComponent.Builder()
                                            .setSpawnRate(48D).setMaxParticleCount(100D))
                                    .addEmitterComponent(new EmitterLifetimeLoopingComponent.Builder().setActiveTime(1D))
                                    .addEmitterComponent(new EmitterShapeSphereComponent.Builder()
                                            .setRadius(0.5D).setSurfaceOnly(true).setDirection("outwards"))
                                    .addParticleComponent(new ParticleLifetimeExpressionComponent.Builder().setMaxLifetime(RawMolangValue.compileExpression("math.random(0.2, 0.4)")))
                                    .addParticleComponent(new ParticleInitialSpeedComponent.Builder().setInitialSpeed(5D))
                                    .addParticleComponent(new ParticleMotionDynamicComponent.Builder())
                                    .addParticleComponent(new AppearanceBillboardComponent.Builder()
                                            .setSize(0.2D, 0.2D).setCameraMode(ParticleCameraMode.ROTATE_XYZ)
                                            .setTextureWidth(128).setTextureHeight(128)
                                            .setUv(0D, 24D).setUvSize(8D, 8D)),
                            "straightCreatureRaySource",
                            (Entity & IAnimatable<?>) creature.getAnimationData().getHolder()
                    );
                })
                .setOnMoveEndEffect(creature -> RiftLibParticleHelper.killParticle(
                        (Entity & IAnimatable<?>) creature.getAnimationData().getHolder(),
                        "prift:fire_fang", "straightCreatureRaySource"
                ))
                .setUseLookBasedFacing()
                .setOnMoveHitEffect(creature -> {
                    int burnChance = creature.getEntityWorld().rand.nextInt(4);
                    creature.createOffenseHitboxesByTag(
                            "jaw", 4, entity -> {
                                if (burnChance == 0) entity.setFire(5);
                            }
                    );
                })
                .setCooldown(100)
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("fire_fang");
            this.setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                    .addDetectionRule(new AnimatedHitboxDetectionRule("jaw", CreatureLookUtil::captureHeadAttackLook))
                    .setUseBlockBreak();
        }
    }
}
