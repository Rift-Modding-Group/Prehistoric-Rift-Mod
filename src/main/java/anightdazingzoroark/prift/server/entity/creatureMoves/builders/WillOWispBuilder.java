package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HorizontalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.VerticalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.api.projectile.ProjectileTrail;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleMaterial;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterSteadyComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapePointComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceTintingComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState.ParticleInitialSpeedComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime.ParticleLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleMotion.ParticleMotionDynamicComponent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class WillOWispBuilder extends CreatureMoveBuilder {
    public WillOWispBuilder() {
        this.setStaminaCost(0.02f)
                .setRequireFindTargetToUse()
                .setElement(Element.FIRE)
                .setMoveType(MoveType.STATUS)
                .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureHeadAttackLook(creature, "willowisp"))
                .setOnMoveHitEffect(creature -> {
                    ProjectileBuilder fireballProjectile = new ProjectileBuilder().setName("willowisp")
                            .setRotateAlongPitch()
                            .setProjectileTrail(new ParticleBuilder()
                                    .setTexture(new ResourceLocation("textures/particle/particles.png"))
                                    .setMaterial(ParticleMaterial.ALPHA)
                                    .addEmitterComponent(new EmitterSteadyComponent.Builder().setSpawnRate(24).setMaxParticleCount(100))
                                    .addEmitterComponent(new EmitterLifetimeExpressionComponent.Builder().setActivationExpression(1D))
                                    .addEmitterComponent(new EmitterShapePointComponent.Builder().setDirection(0, 1, 0))
                                    .addParticleComponent(new ParticleLifetimeExpressionComponent.Builder().setMaxLifetime(2D))
                                    .addParticleComponent(new ParticleInitialSpeedComponent.Builder().setInitialSpeed(0.8D))
                                    .addParticleComponent(new ParticleMotionDynamicComponent.Builder())
                                    .addParticleComponent(ProjectileTrail.makeAppearanceForParticleTrail()
                                            .setSize(0.2D, 0.2D).setUv(0, 48).setUvSize(16, 16)
                                    )
                            )
                            .setOnImpactFromCreatureEffect((iCreature, projectile, hitEntity, hitPos) -> {
                                if (hitEntity != null) hitEntity.setFire(15);
                            });

                    //when ridden, shoot forward
                    if (creature.getControllingPassenger() != null) {
                        Vec3d creaturePos = creature.getPositionVector();
                        creature.getEntityWorld().playSound(
                                null, creaturePos.x, creaturePos.y, creaturePos.z,
                                RiftSounds.getSound("generic_move.fire"),
                                SoundCategory.NEUTRAL, 1f, 1f
                        );
                        for (int i = 0; i < 4; i++) creature.launchProjectile(fireballProjectile, 3f, 16f);
                    }
                    //otherwise, hit target
                    else {
                        EntityLivingBase target = creature.getAttackTarget();
                        if (target == null || !target.isEntityAlive()) return;

                        Vec3d creaturePos = creature.getPositionVector();
                        creature.getEntityWorld().playSound(
                                null, creaturePos.x, creaturePos.y, creaturePos.z,
                                RiftSounds.getSound("generic_move.fire"),
                                SoundCategory.NEUTRAL, 1f, 1f
                        );
                        for (int i = 0; i < 4; i++) creature.launchProjectile(fireballProjectile, target, 3f, 16f);
                    }
                })
                .setUseLookBasedFacing()
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.RANGED)
                .setRiddenPitchBounds(-75f, 35f)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.POINT)
                .setCooldown(400);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("willowisp");
            this.setPriorityPredicate((creature, target) -> {
                        return (target != null && target.isEntityAlive()) ? 0 : -1;
                    })
                    .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(12D, 16D))
                    .addDetectionRule(new VerticalDistanceFromUserDetectionRule(4D, 16D))
                    .setDontPathToTarget();
        }
    }
}
