package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HitboxDistanceFromUserDetectionRule;
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
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HydroPumpBuilder extends CreatureMoveBuilder {
    public HydroPumpBuilder() {
        this.setStaminaCost(0.08f)
                .setStaminaDrainPerSecond(0.0125f)
                .setBasePower(90)
                .setRequireFindTargetToUse()
                .setMoveType(CreatureMoveBuilder.MoveType.SPECIAL)
                .setElement(Element.WATER)
                .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.initializeRayLook(creature, target, "straightCreatureRaySource"))
                .setWhileMoveUseEffect((creature, target) -> {
                    if (creature.getControllingPassenger() == null && (target == null || !target.isEntityAlive())) return;
                    CreatureLookUtil.updateRayLook(creature, target, "straightCreatureRaySource");
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
                                    new ParticleBuilder().setIdentifier("prift:hydro_pump_stream")
                                            .setTexture(new ResourceLocation("textures/particle/particles.png"))
                                            .setMaterial(ParticleMaterial.ALPHA)
                                            .addEmitterComponent(new EmitterSteadyComponent.Builder()
                                                    .setSpawnRate(96D).setMaxParticleCount(220D))
                                            .addEmitterComponent(new EmitterLifetimeExpressionComponent.Builder()
                                                    .setActivationExpression(1D).setExpirationExpression(0D))
                                            .addEmitterComponent(new EmitterShapePointComponent.Builder().setDirection(
                                                    new Constant(0D),
                                                    new Constant(0D),
                                                    new Constant(-1D)
                                            ))
                                            .addParticleComponent(new ParticleLifetimeExpressionComponent.Builder()
                                                    .setMaxLifetime("math.random(0.95, 1.15)"))
                                            .addParticleComponent(new AppearanceTintingComponent.Builder()
                                                    .setColor(0x2b42d9, false)
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
                                    new RiftLibRayBuilder("prift:hydro_pump")
                                            .setMotionOnly()
                                            .setMovementShape(() -> new RiftLibRayConeMotionShape(0.1D, 0.35D, 0.25D, 3, 12, false, false))
                                            .setMotionSpeed(1D)
                                            .setMaxMotionDistance(16D)
                                            .setBlockBreakCheck((rayCreator, pos) -> {
                                                World world = rayCreator.world;
                                                IBlockState blockState = world.getBlockState(pos);
                                                return blockState.getBlock().isFlammable(world, pos, EnumFacing.UP);
                                            })
                                            .setHitHandler((rayCreator, rayOrigin, rayHitResult) -> {
                                                for (Entity hitEntity : rayHitResult.hitEntities()) {
                                                    if (creature.isRelatedToEntity(hitEntity)) continue;
                                                    if (hitEntity instanceof EntityLivingBase) creature.attackEntityAsMob(hitEntity);
                                                }
                                            }),
                                    "straightCreatureRaySource"
                            );

                            RiftLibSoundHelper.playLoopingSound(
                                    creature.getAnimationData().getHolder(),
                                    RiftSounds.getSound("hydro_pump.use_loop"),
                                    SoundCategory.NEUTRAL
                            );
                        })
                        .setReleaseEndEffect(creature -> {
                            RiftLibSoundHelper.stopLoopingSound(
                                    creature.getAnimationData().getHolder(),
                                    RiftSounds.getSound("hydro_pump.use_loop"),
                                    SoundCategory.NEUTRAL
                            );
                            RiftLibParticleHelper.killParticle(
                                    (Entity & IAnimatable<?>) creature.getAnimationData().getHolder(),
                                    "prift:hydro_pump_stream", "straightCreatureRaySource"
                            );
                            RiftLibRayHelper.killRay(creature.getAnimationData().getHolder(), "prift:hydro_pump");
                        })
                )
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.RANGED)
                .setRiddenAimLocator("straightCreatureRaySource", 16D)
                .setRiddenPitchBounds(-75f, 35f)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.POINT);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("hydro_pump", MoveCategory.RANGED);
            this.setPriorityPredicate((creature, target) -> target != null && target.isEntityAlive())
                    .addDetectionRule(new HitboxDistanceFromUserDetectionRule(4D));
        }
    }
}
