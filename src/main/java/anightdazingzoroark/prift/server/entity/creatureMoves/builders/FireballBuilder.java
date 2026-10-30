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
import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleMaterial;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterSteadyComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapeDiscComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapePointComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState.ParticleInitialSpeedComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime.ParticleLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleMotion.ParticleMotionDynamicComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public class FireballBuilder extends CreatureMoveBuilder {
    public FireballBuilder() {
        this.setStaminaCost(0.08f)
                .setBasePower(50)
                .setRequireFindTargetToUse()
                .setElement(Element.FIRE)
                .setMoveType(CreatureMoveBuilder.MoveType.SPECIAL)
                .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureHeadAttackLook(creature, "fireball"))
                .setOnMoveHitEffect(creature -> {
                    ProjectileBuilder fireballProjectile = new ProjectileBuilder().setName("fireball")
                            .setProjectileModel(ProjectileBuilder.ProjectileModel.FLAT)
                            .setProjectileTrail(new ParticleBuilder()
                                    .setTexture(new ResourceLocation("textures/particle/particles.png"))
                                    .setMaterial(ParticleMaterial.ALPHA)
                                    .addEmitterComponent(new EmitterSteadyComponent.Builder().setSpawnRate(24).setMaxParticleCount(100))
                                    .addEmitterComponent(new EmitterLifetimeExpressionComponent.Builder().setActivationExpression(1D))
                                    .addEmitterComponent(new EmitterShapeDiscComponent.Builder().setDirection(
                                            RawMolangValue.compileExpression("math.random(-0.15, 0.15)"),
                                            RawMolangValue.compileExpression("math.random(-0.15, 0.15)"),
                                            new Constant(-1D)
                                    ).setRadius(0.8D).setPlaneNormal(0, 0, 1D))
                                    .addParticleComponent(new ParticleLifetimeExpressionComponent.Builder().setMaxLifetime(0.7D))
                                    .addParticleComponent(new ParticleInitialSpeedComponent.Builder().setInitialSpeed(0.8D))
                                    .addParticleComponent(new ParticleMotionDynamicComponent.Builder())
                                    .addParticleComponent(ProjectileTrail.makeAppearanceForParticleTrail()
                                            .setSize(0.2D, 0.2D).setUv(0, 48).setUvSize(16, 16)
                                    )
                            )
                            .setOnImpactFromCreatureEffect((iCreature, projectile, hitEntity, hitPos) -> {
                                World world = projectile.getEntityWorld();
                                if (world.isRemote || !ForgeEventFactory.getMobGriefingEvent(world, iCreature.getAnimationData().getHolder())) return;

                                BlockPos firePos;
                                if (hitEntity != null) firePos = hitEntity.getPosition();
                                else {
                                    Vec3d movement = projectile.getMotionVector();
                                    RayTraceResult impact = world.rayTraceBlocks(hitPos, hitPos.add(movement).add(movement.normalize().scale(0.1D)));
                                    if (impact == null || impact.typeOfHit != RayTraceResult.Type.BLOCK || impact.sideHit == null) return;
                                    firePos = impact.getBlockPos().offset(impact.sideHit);
                                }

                                for (BlockPos position : BlockPos.getAllInBoxMutable(firePos.add(-3, -3, -3), firePos.add(3, 3, 3))) {
                                    if (firePos.distanceSq(position) <= 9D
                                            && world.isAirBlock(position)
                                            && Blocks.FIRE.canPlaceBlockAt(world, position)
                                    ) {
                                        world.setBlockState(position, Blocks.FIRE.getDefaultState(), 3);
                                    }
                                }
                            });

                    //when ridden, shoot forward
                    if (creature.getControllingPassenger() != null) {
                        Vec3d creaturePos = creature.getPositionVector();
                        creature.getEntityWorld().playSound(
                                null, creaturePos.x, creaturePos.y, creaturePos.z,
                                RiftSounds.getSound("generic_move.fire"),
                                SoundCategory.NEUTRAL, 1f, 1f
                        );
                        creature.launchProjectile(fireballProjectile, 3f, 0f);
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
                        creature.launchProjectile(fireballProjectile, target, 3f, 0f);
                    }
                })
                .setUseLookBasedFacing()
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.RANGED)
                .setRiddenPitchBounds(-75f, 35f)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.POINT)
                .setCooldown(100);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("fireball");
            this.setPriorityPredicate((creature, target) -> {
                        return (target != null && target.isEntityAlive()) ? 0 : -1;
                    })
                    .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(12D, 16D))
                    .addDetectionRule(new VerticalDistanceFromUserDetectionRule(4D, 16D))
                    .setDontPathToTarget();
        }
    }
}
