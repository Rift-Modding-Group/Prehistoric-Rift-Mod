package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.ICreature;
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
import anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState.ParticleInitialSpeedComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime.ParticleLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleMotion.ParticleMotionDynamicComponent;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import anightdazingzoroark.riftlib.ray.RiftLibRayHelper;
import anightdazingzoroark.riftlib.ray.rayShape.motion.RiftLibRayBoxMotionShape;
import anightdazingzoroark.riftlib.ray.rayShape.motion.RiftLibRayConeMotionShape;
import anightdazingzoroark.riftlib.ray.rayShape.motion.RiftLibRayStraightMotionShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SonicBlastBuilder extends CreatureMoveBuilder {
    public SonicBlastBuilder() {
        this.setStaminaCost(0.2f)
                .setStaminaDrainPerSecond(0.05f)
                .setBasePower(90)
                .setRequireFindTargetToUse()
                .setMoveType(MoveType.SPECIAL)
                .setElement(Element.SONIC)
                .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureRayLook(creature, target, "flamethrowerAimLocator"))
                .setWhileMoveUseEffect((creature, target) -> {
                    if (!creature.getAnimationData().getHolder().isBeingRidden() && (target == null || !target.isEntityAlive())) return;
                    CreatureLookUtil.captureRayLook(creature, target, "flamethrowerAimLocator");
                })
                .setMoveChargeupBuilder(new CreatureMoveChargeupBuilder()
                        .setChargeUpWhileUse(true)
                        .setCanLookWhileUsing(true)
                        .setMaxChargeUp(300)
                        .setPrereleaseEndEffect(creature -> {
                            //create the particles
                            /*
                            RiftLibParticleHelper.createParticle(
                                    new ParticleBuilder().setIdentifier("prift:flamethrower_fire")
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
                                            .addParticleComponent(new ParticleInitialSpeedComponent.Builder().setInitialSpeed(14D))
                                            .addParticleComponent(new ParticleMotionDynamicComponent.Builder())
                                            .addParticleComponent(new AppearanceBillboardComponent.Builder()
                                                    .setSize(0.2D, 0.2D).setCameraMode(ParticleCameraMode.ROTATE_XYZ)
                                                    .setTextureWidth(128).setTextureHeight(128)
                                                    .setUv(0D, 24D).setUvSize(8D, 8D)),
                                    "flamethrowerAimLocator",
                                    (Entity & IAnimatable<?>) creature.getAnimationData().getHolder()
                            );
                             */

                            //create the ray
                            creature.createRay(
                                    new RiftLibRayBuilder("prift:sonic_blast")
                                            .setMotionOnly()
                                            .setMovementShape(() -> new RiftLibRayConeMotionShape(0.1D, 0.35D, 0.25D, 3, 12, false, false))
                                            .setMotionSpeed(1D)
                                            .setMaxMotionDistance(16D)
                                            .setBlockBreakCheck((rayCreator, pos) -> {
                                                World world = rayCreator.world;
                                                IBlockState blockState = world.getBlockState(pos);
                                                float blockHardness = blockState.getBlockHardness(world, pos);
                                                return blockHardness <= 1 && blockHardness > 0;
                                            })
                                            .setHitHandler((rayCreator, rayOrigin, rayHitResult) -> {
                                                if (!(rayCreator instanceof ICreature source)) return;
                                                for (Entity hitEntity : rayHitResult.hitEntities()) {
                                                    if (hitEntity instanceof EntityLivingBase) source.attackEntityAsMob(hitEntity);
                                                }
                                            }),
                                    "flamethrowerAimLocator"
                            );
                        })
                        .setReleaseEndEffect(creature -> {
                            /*
                            RiftLibParticleHelper.killParticle(
                                    (Entity & IAnimatable<?>) creature.getAnimationData().getHolder(),
                                    "prift:flamethrower_fire", "flamethrowerAimLocator"
                            );
                             */
                            RiftLibRayHelper.killRay(creature.getAnimationData().getHolder(), "prift:sonic_blast");
                        })
                )
                .setRiddenAimingType(RiddenAimingType.RANGED)
                .setRiddenPitchBounds(-75f, 35f)
                .setRiddenCrosshairType(RiddenCrosshairType.POINT);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("sonic_blast");
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