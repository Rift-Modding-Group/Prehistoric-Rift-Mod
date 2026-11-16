package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleCameraMode;
import anightdazingzoroark.riftlib.particle.ParticleMaterial;
import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeLoopingComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterSteadyComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapeDiscComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapePointComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceBillboardComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState.ParticleInitialSpeedComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime.ParticleLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleMotion.ParticleMotionDynamicComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class BideBuilder extends CreatureMoveBuilder {
    public BideBuilder() {
        this.setMakesContact()
                .setElement(Element.GUARD)
                .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                .setRequireFindTargetToUse()
                .setStaminaCost(0.08f)
                .setStaminaDrainPerSecond(0.02f)
                .setPreventMovement()
                .setOnMoveBeginEffect((creature, target) -> {
                    creature.setProperty("BideReceivedDamage", 0D);

                    RiftLibParticleHelper.createParticle(
                            new ParticleBuilder().setIdentifier("prift:bide_rage")
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
                                            .setUv(8D, 40D).setUvSize(8D, 8D)),
                            "centerPoint",
                            (Entity & IAnimatable<?>) creature.getAnimationData().getHolder()
                    );
                })
                .setOnMoveHitEffect(creature -> {
                    double releasedDamage = (double) creature.getProperty("BideReceivedDamage").getValue() * 2D;
                    creature.affectEntitiesWithinBounds(4, entity -> {
                        entity.attackEntityFrom(
                                DamageSource.causeMobDamage((EntityLivingBase) creature),
                                (float) releasedDamage
                        );
                    });

                    Vec3d creaturePos = creature.getPositionVector();
                    creature.getEntityWorld().playSound(
                            null,
                            creaturePos.x, creaturePos.y, creaturePos.z,
                            RiftSounds.getSound("generic_move.bide_release"),
                            SoundCategory.NEUTRAL,
                            1f, 1f
                    );
                })
                .setOnMoveEndEffect(creature -> {
                    creature.setProperty("BideReceivedDamage", 0D);
                    RiftLibParticleHelper.killParticle(
                            (Entity & IAnimatable<?>) creature.getAnimationData().getHolder(),
                            "prift:bide_rage", "centerPoint"
                    );
                })
                .setMoveChargeupBuilder(new CreatureMoveChargeupBuilder()
                        .setChargeType(CreatureMoveChargeupBuilder.ChargeType.CHARGE_UP_THEN_RELEASE)
                        .setReleaseOnEndWindup()
                        .setMaxChargeUp(100)
                );
    }
}