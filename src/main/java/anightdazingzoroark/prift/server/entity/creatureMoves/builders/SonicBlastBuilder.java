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
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
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
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
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
                            RiftLibParticleHelper.createParticle(
                                    new ParticleBuilder().setIdentifier("prift:sonic_blast")
                                            .setTexture(new ResourceLocation(RiftLib.ModID, "textures/particle/satellite_signal.png")) //lol
                                            .setMaterial(ParticleMaterial.ALPHA)
                                            .addEmitterComponent(new EmitterSteadyComponent.Builder()
                                                    .setSpawnRate(20D).setMaxParticleCount(40D))
                                            .addEmitterComponent(new EmitterLifetimeExpressionComponent.Builder()
                                                    .setActivationExpression(1D).setExpirationExpression(0D))
                                            .addEmitterComponent(new EmitterShapePointComponent.Builder().setDirection(
                                                    0, 0, -1D
                                            ))
                                            .addParticleComponent(new ParticleLifetimeExpressionComponent.Builder()
                                                    .setMaxLifetime(0.8D))
                                            .addParticleComponent(new ParticleInitialSpeedComponent.Builder().setInitialSpeed(14D))
                                            .addParticleComponent(new ParticleMotionDynamicComponent.Builder())
                                            .addParticleComponent(new AppearanceBillboardComponent.Builder()
                                                    .setSize(
                                                            RawMolangValue.compileExpression("0.3 + variable.particle_age * 1.5"),
                                                            RawMolangValue.compileExpression("0.3 + variable.particle_age * 1.5")
                                                    )
                                                    .setCameraMode(ParticleCameraMode.ROTATE_XYZ)
                                                    .setTextureWidth(16).setTextureHeight(16)
                                                    .setUv(0D, 0D).setUvSize(16D, 16D))
                                            .addParticleComponent(new AppearanceTintingComponent.Builder().setColor(0x00F9FF, false)),
                                    "flamethrowerAimLocator",
                                    (Entity & IAnimatable<?>) creature.getAnimationData().getHolder()
                            );

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

                            RiftLibSoundHelper.playLoopingSound(
                                    creature.getAnimationData().getHolder(),
                                    RiftSounds.getSound("sonic_blast.use_loop"),
                                    SoundCategory.NEUTRAL
                            );
                        })
                        .setReleaseEndEffect(creature -> {
                            RiftLibSoundHelper.stopLoopingSound(
                                    creature.getAnimationData().getHolder(),
                                    RiftSounds.getSound("sonic_blast.use_loop"),
                                    SoundCategory.NEUTRAL
                            );
                            RiftLibParticleHelper.killParticle(
                                    (Entity & IAnimatable<?>) creature.getAnimationData().getHolder(),
                                    "prift:sonic_blast", "flamethrowerAimLocator"
                            );
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