package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.api.creature.CreatureMoveRegistry;
import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder.ChargeupPhase;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.BoundingBoxDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.DistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HorizontalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.VerticalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.api.projectile.ProjectileTrail;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.prift.api.util.CreatureMoveUtil;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.server.properties.OtherEntityProperties;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityLiving;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.model.AnimationPoseSampler;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceTintingComponent;
import anightdazingzoroark.riftlib.ray.RiftLibRay;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import anightdazingzoroark.riftlib.ray.RiftLibRayHelper;
import anightdazingzoroark.riftlib.ray.rayShape.impact.RiftLibRayEllipsoidImpactShape;
import anightdazingzoroark.riftlib.ray.rayShape.impact.RiftLibRaySphereImpactShape;
import anightdazingzoroark.riftlib.ray.rayShape.motion.RiftLibRayConeMotionShape;

import java.util.List;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Shared built-in move behavior and AI rules.
 */
public class RiftMoves {
    private static boolean registered;
    private static double FLAMETHROWER_MAX_MOTION_DISTANCE = 16D;

    public static void registerMoves() {
        if (registered) return;
        DistanceFromUserDetectionRule peckRange = new DistanceFromUserDetectionRule(2D);

        CreatureMoveRegistry.registerMove("placeholder", new CreatureMoveBuilder().setMoveType(CreatureMoveBuilder.MoveType.STATUS));

        CreatureMoveRegistry.registerMove("bite",
                CreatureMoveCommon.standardMeleeMove.copy()
                        .setBasePower(50)
                        .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureHeadAttackLook(creature, "bite", "jaw", 4D))
                        .setUseLookBasedFacing()
                        .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                        .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE),
                new MoveRuleBuilder("bite")
                        .setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                        .addDetectionRule(new AnimatedHitboxDetectionRule("jaw", creature -> CreatureLookUtil.captureHeadAttackLook(creature, "bite", "jaw", 4D)))
                        .setUseBlockBreak()
        );

        CreatureMoveRegistry.registerMove("stomp",
                new CreatureMoveBuilder()
                        .setStaminaCost(0.02f)
                        .setBasePower(30)
                        .setRequireFindTargetToUse()
                        .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                        .setOnMoveHitEffect(creature -> creature.createRay(
                                new RiftLibRayBuilder("prift:stomp")
                                        .setImpactOnly()
                                        .setImpactShape(() -> new RiftLibRayEllipsoidImpactShape(1D, 0.2D, 1D).topOnly())
                                        .setMaxMotionDistance(5D)
                                        .setOnlyOneSegment()
                                        .setMotionSpeed(1.5D)
                                        .setHitHandler((rayCreator, rayOrigin, rayHitResult) -> {
                                            for (Entity hitEntity : rayHitResult.hitEntities()) {
                                                if (hitEntity instanceof EntityLivingBase) creature.attackEntityAsMob(hitEntity);
                                            }
                                        }),
                                "stompOrigin"
                        )),
                new MoveRuleBuilder("stomp")
                        .setPriorityPredicate((creature, target) -> {
                            if (target != null && target.isEntityAlive() && !creature.bodyTouchingLiquid()
                                    && creature.isOnGround()
                                    && creature.aabbIntersectsBoundingBox(target.getEntityBoundingBox(), "stompHitZone")) {
                                return 0;
                            }
                            return -1;
                        })
                        .addDetectionRule(new BoundingBoxDetectionRule("stompHitZone"))
                        .setDontPathToTarget()
        );

        CreatureMoveRegistry.registerMove("flame_spit",
                new CreatureMoveBuilder()
                        .setStaminaCost(0.15f)
                        .setBasePower(40)
                        .setRequireFindTargetToUse()
                        .setElement(Element.FIRE)
                        .setMoveType(CreatureMoveBuilder.MoveType.SPECIAL)
                        .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureHeadAttackLook(creature, "flame_spit", "flame_spit", 8.75D))
                        .setUseLookBasedFacing()
                        .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                        .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE)
                        .setCooldown(300),
                new MoveRuleBuilder("flame_spit")
                        .setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                        .addDetectionRule(new AnimatedHitboxDetectionRule("flame_spit", creature -> CreatureLookUtil.captureHeadAttackLook(creature, "flame_spit", "flame_spit", 8.75D)))
                        .setUseBlockBreak()
        );

        CreatureMoveRegistry.registerMove("power_roar",
                new CreatureMoveBuilder()
                        .setStaminaCost(0.15f)
                        .setBasePower(20)
                        .setRequireFindTargetToUse()
                        .setMoveType(CreatureMoveBuilder.MoveType.SPECIAL)
                        .setElement(Element.SONIC)
                        .setOnMoveHitEffect(creature -> creature.createRay(
                                new RiftLibRayBuilder("prift:power_roar")
                                        .setImpactOnly()
                                        .setImpactShape(() -> new RiftLibRaySphereImpactShape().topOnly())
                                        .setMaxMotionDistance(12D)
                                        .setMotionSpeed(1.5D)
                                        .setBlockBreakCheck((rayCreator, blockPos) -> {
                                            IBlockState blockState = rayCreator.world.getBlockState(blockPos);
                                            float hardness = blockState.getBlockHardness(rayCreator.world, blockPos);
                                            return hardness <= 1f && hardness >= 0f;
                                        })
                                        .setImpactCreationInterval(5)
                                        .setHitHandler((rayCreator, rayOrigin, rayHitResult) -> {
                                            for (Entity hitEntity : rayHitResult.hitEntities()) {
                                                if (hitEntity instanceof EntityLivingBase) creature.attackEntityAsMob(hitEntity);
                                            }
                                        }),
                                "centerPoint"
                        ))
                        .setOnMoveEndEffect(creature -> RiftLibRayHelper.killRay(
                                creature.getAnimationData().getHolder(), "prift:power_roar"))
                        .setCooldown(1200),
                new MoveRuleBuilder("power_roar")
                        .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(12D))
                        .setUseWhenFrustrated()
                        .setDontPathToTarget()
        );

        CreatureMoveRegistry.registerMove("flamethrower",
                new CreatureMoveBuilder()
                        .setStaminaCost(0.08f)
                        .setStaminaDrainPerSecond(0.0125f)
                        .setBasePower(90)
                        .setRequireFindTargetToUse()
                        .setMoveType(CreatureMoveBuilder.MoveType.SPECIAL)
                        .setElement(Element.FIRE)
                        .setOnMoveBeginEffect(RiftMoves::captureFlamethrowerLook)
                        .setWhileMoveUseEffect((creature, target) -> {
                            if (!creature.getAnimationData().getHolder().isBeingRidden() && (target == null || !target.isEntityAlive()))
                                return;
                            captureFlamethrowerLook(creature, target);
                        })
                        .setMoveChargeupBuilder(new CreatureMoveChargeupBuilder()
                                .setChargeUpWhileUse(true)
                                .setCanLookWhileUsing(true)
                                .setMaxChargeUp(300)
                                .setPrereleaseEndEffect(creature -> {
                                    creature.createRay(
                                            new RiftLibRayBuilder("prift:flamethrower")
                                                    .setMotionOnly()
                                                    .setMovementShape(() -> new RiftLibRayConeMotionShape(0.1D, 0.35D, 0.25D, 3, 12, false, false))
                                                    .setMotionSpeed(1D)
                                                    .setMaxMotionDistance(FLAMETHROWER_MAX_MOTION_DISTANCE)
                                                    .setBlockBreakCheck((rayCreator, pos) -> {
                                                        World world = rayCreator.world;
                                                        IBlockState blockState = world.getBlockState(pos);
                                                        return blockState.getBlock().isFlammable(world, pos, EnumFacing.UP);
                                                    })
                                                    .setHitHandler((rayCreator, rayOrigin, rayHitResult) -> {
                                                        for (Entity hitEntity : rayHitResult.hitEntities()) {
                                                            if (hitEntity instanceof EntityLivingBase) creature.attackEntityAsMob(hitEntity);
                                                        }
                                                        World world = rayCreator.world;
                                                        for (BlockPos hitBlockPos : rayHitResult.hitBlockPositions()) {
                                                            if (world.getBlockState(hitBlockPos).getBlock() == Blocks.SNOW_LAYER
                                                                    && Blocks.FIRE.canPlaceBlockAt(world, hitBlockPos)
                                                                    && creature.getRNG().nextInt(4) == 0) {
                                                                world.setBlockState(hitBlockPos, Blocks.FIRE.getDefaultState(), 3);
                                                            } else if (!world.isAirBlock(hitBlockPos)
                                                                    && world.isAirBlock(hitBlockPos.up())
                                                                    && Blocks.FIRE.canPlaceBlockAt(world, hitBlockPos.up())
                                                                    && creature.getRNG().nextInt(4) == 0) {
                                                                world.setBlockState(hitBlockPos.up(), Blocks.FIRE.getDefaultState(), 3);
                                                            }
                                                        }
                                                    }),
                                            "flamethrowerAimLocator"
                                    );
                                })
                                .setReleaseEndEffect(creature -> {
                                    RiftLibRayHelper.killRay(creature.getAnimationData().getHolder(), "prift:flamethrower");
                                })
                        )
                        .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.RANGED)
                        .setRiddenPitchBounds(-75f, 35f)
                        .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.POINT),
                new MoveRuleBuilder("flamethrower")
                        .setPriorityPredicate((creature, target) -> {
                            if (target != null && target.isEntityAlive() && (creature.atRageThreshold() || creature.isUnableToPathToTarget())) {
                                return 0;
                            }
                            return -1;
                        })
                        .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(6D, 16D))
                        .addDetectionRule(new VerticalDistanceFromUserDetectionRule(5D, 16D))
                        .setUseWhenFrustrated()
        );

        CreatureMoveRegistry.registerMove("tail_stab",
                CreatureMoveCommon.standardMeleeMove.copy()
                        .setBasePower(50)
                        .setOnMoveBeginEffect(RiftMoves::captureTailStabLook)
                        .setUseLookBasedFacing()
                        .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                        .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE),
                new MoveRuleBuilder("tail_stab")
                        .setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                        .addDetectionRule(new AnimatedHitboxDetectionRule("thagomizer", RiftMoves::captureTailStabLook))
                        .setUseBlockBreak()
        );

        CreatureMoveRegistry.registerMove("tail_slam",
                new CreatureMoveBuilder()
                        .setMakesContact()
                        .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                        .setRequireFindTargetToUse()
                        .setStaminaCost(0.15f)
                        .setBasePower(70)
                        .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                        .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE)
                        .setCooldown(400),
                new MoveRuleBuilder("tail_slam")
                        .setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                        .addDetectionRule(new BoundingBoxDetectionRule("frontZone", true))
                        .setUseBlockBreak()
        );

        CreatureMoveRegistry.registerMove("thagomize",
                new CreatureMoveBuilder()
                        .setMakesContact()
                        .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                        .setRequireFindTargetToUse()
                        .setStaminaCost(0.08f)
                        .setStaminaDrainPerSecond(0.02f)
                        .setBasePower(50)
                        .setPreventMovement()
                        .setUseLookBasedFacing()
                        .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                        .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE)
                        .setOnMoveBeginEffect(RiftMoves::captureThagomizeLook)
                        .setOnMoveEndEffect(creature -> creature.setProperty("ChargeUpLevel", 0))
                        .setMoveChargeupBuilder(new CreatureMoveChargeupBuilder()
                                .setChargeUpThenRelease()
                                .setCanRotateWhileCharging(true)
                                .setWindupEndEffect((creature, charge) -> captureThagomizeLook(creature, creature.getAttackTarget()))
                                .setMaxChargeUp(100)
                                .setCooldownMultiplier(6D)
                                .setBasePowerMultiplier((creature, basePower, chargeUp) -> {
                                    int chargeIntMultiplier = (int) Math.floor(chargeUp / 20D) + 1;
                                    return MathUtil.slopeResult(chargeIntMultiplier, true, 1, 6, 1D, 2D);
                                })
                                .setWindupEffect((creature, chargeUp) -> {
                                    int chargeUpLevel = (int) creature.getProperty("ChargeUpLevel").getValue();
                                    if (chargeUpLevel >= 5 || chargeUp <= 0 || chargeUp > 100 || chargeUp % 20D != 0)
                                        return;

                                    //set chargeup level
                                    chargeUpLevel++;
                                    creature.setProperty("ChargeUpLevel", chargeUpLevel);

                                    //show particles
                                    AxisAlignedBB creatureAABB = creature.getEntityBoundingBox();
                                    double width = creatureAABB.maxX - creatureAABB.minX;
                                    double height = creatureAABB.maxY - creatureAABB.minY;
                                    RiftLibParticleHelper.createParticle(
                                            "prift:charge_level_up",
                                            creature.getPositionVector().x, creature.getPositionVector().y + height / 2D, creature.getPositionVector().z,
                                            0, 0,
                                            "variable.emission_radius", Double.toString(width * 0.005D)
                                    );

                                    //play sound
                                    if (chargeUpLevel == 1 || chargeUpLevel == 2) {
                                        creature.getEntityWorld().playSound(
                                                null,
                                                creature.getPositionVector().x,
                                                creature.getPositionVector().y,
                                                creature.getPositionVector().z,
                                                RiftSounds.getSound("generic_move.chargeup_weak"),
                                                SoundCategory.NEUTRAL,
                                                1f, 1f
                                        );
                                    } else if (chargeUpLevel == 3 || chargeUpLevel == 4) {
                                        creature.getEntityWorld().playSound(
                                                null,
                                                creature.getPositionVector().x,
                                                creature.getPositionVector().y,
                                                creature.getPositionVector().z,
                                                RiftSounds.getSound("generic_move.chargeup_medium"),
                                                SoundCategory.NEUTRAL,
                                                1f, 1f
                                        );
                                    } else if (chargeUpLevel == 5) {
                                        creature.getEntityWorld().playSound(
                                                null,
                                                creature.getPositionVector().x,
                                                creature.getPositionVector().y,
                                                creature.getPositionVector().z,
                                                RiftSounds.getSound("generic_move.chargeup_strong"),
                                                SoundCategory.NEUTRAL,
                                                1f, 1f
                                        );
                                    }
                                })
                                .setOnHitEntityDuringRelease((creature, chargeUp, hitEntity) -> {
                                    if (!(hitEntity instanceof EntityLivingBase hitEntityLiving)) return;
                                    OtherEntityProperties otherEntityProperties = OtherEntityProperties.get(hitEntityLiving);
                                    if (otherEntityProperties == null) return;
                                    int bleedStrength = (int) MathUtil.slopeResult(chargeUp, true, 0, 100, 0, 4);
                                    otherEntityProperties.setBleeding(bleedStrength, 100);
                                })
                        ),
                new MoveRuleBuilder("thagomize")
                        .setPriorityPredicate((creature, target) -> {
                            return target != null && target.isEntityAlive() && creature.atRageThreshold() ? 0 : -1;
                        })
                        .addDetectionRule(new AnimatedHitboxDetectionRule("thagomizer", RiftMoves::captureThagomizeLook))
        );

        CreatureMoveRegistry.registerMove("plate_fling",
                new CreatureMoveBuilder()
                        .setStaminaCost(0.08f)
                        .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                        .setBasePower(30)
                        .setRequireFindTargetToUse()
                        .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.RANGED)
                        .setRiddenPitchBounds(-75f, 35f)
                        .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.POINT)
                        .setOnMoveBeginEffect((creature, target) -> creature.getAnimationData().setVariable("plate_fling_shot", 0D))
                        .setOnMoveHitEffect(creature -> {
                            AnimationDataEntityLiving data = creature.getAnimationData();
                            data.setVariable("plate_fling_pitch", Math.clamp(data.getHeadPitch(1f), -15f, 15f));
                            data.setVariable("plate_fling_yaw", Math.clamp(data.getHeadYaw(1f), -15f, 15f));
                            data.setVariable("plate_fling_shot", 1D);

                            //when ridden, shoot forward
                            if (creature.getControllingPassenger() != null) {
                                creature.launchProjectile(
                                        new ProjectileBuilder().setName("thrown_stegosaurus_plate").setRotateAlongPitch(),
                                        3f, 0f
                                );
                            }
                            //otherwise, hit target
                            else {
                                EntityLivingBase target = creature.getAttackTarget();
                                if (target == null || !target.isEntityAlive()) return;

                                creature.launchProjectile(
                                        new ProjectileBuilder().setName("thrown_stegosaurus_plate").setRotateAlongPitch(),
                                        target, 3f, 0f
                                );
                            }
                        }),
                new MoveRuleBuilder("plate_fling")
                        .setPriorityPredicate((creature, target) -> {
                            return (target != null && target.isEntityAlive()) ? 0 : -1;
                        })
                        .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(12D, 16D))
                        .addDetectionRule(new VerticalDistanceFromUserDetectionRule(4D, 16D))
                        .setDontPathToTarget()
        );

        CreatureMoveRegistry.registerMove("peck",
                CreatureMoveCommon.standardMeleeMove.copy()
                        .setBasePower(40)
                        .setUseLookBasedFacing()
                        .setOnMoveHitEffect(creature -> {
                            EntityLivingBase target = creature.getAttackTarget();
                            if (!creature.getEntityWorld().isRemote && target != null && target.isEntityAlive()
                                    && peckRange.targetWithinRange(creature, target)
                            ) {
                                creature.attackEntityAsMob(target);
                            }
                        }),
                new MoveRuleBuilder("peck")
                        .setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                        .addDetectionRule(peckRange)
        );

        CreatureMoveRegistry.registerMove("headbutt",
                new CreatureMoveBuilder()
                        .setMakesContact()
                        .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                        .setRequireFindTargetToUse()
                        .setStaminaCost(0.02f)
                        .setBasePower(50)
                        .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureHeadAttackLook(creature, "headbutt", "headAttack", 7.5D))
                        .setUseLookBasedFacing()
                        .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                        .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE),
                new MoveRuleBuilder("headbutt")
                        .setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                        .addDetectionRule(new AnimatedHitboxDetectionRule("headAttack", creature -> CreatureLookUtil.captureHeadAttackLook(creature, "headbutt", "headAttack", 7.5D)))
                        .setUseBlockBreak()
        );

        CreatureMoveRegistry.registerMove("charge",
                new CreatureMoveBuilder()
                        .setMakesContact()
                        .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                        .setRequireFindTargetToUse()
                        .setStaminaCost(0.05f)
                        .setBasePower(100)
                        .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.RANGED)
                        .setRiddenPitchBounds(0f, 0f)
                        .setOnMoveBeginEffect((creature, target) -> creature.prepareRush(4D, 10))
                        .setMoveChargeupBuilder(new CreatureMoveChargeupBuilder()
                                .setChargeUpThenRelease()
                                .setCanRotateWhileCharging(true)
                                .setMaxChargeUp(100)
                                .setCooldownMultiplier(4D)
                                .setWindupEndEffect((creature, charge) -> {
                                    int maximumRushTicks = (int) Math.ceil(MathUtil.slopeResult(charge, true, 0, 100, 0, 100));
                                    creature.prepareRush(4D, maximumRushTicks);
                                })
                        ),
                new MoveRuleBuilder("charge")
                        .setPriorityPredicate((creature, target) -> {
                            if (target != null && target.isEntityAlive() && creature.isOnGround()
                                    && !creature.bodyTouchingLiquid() && creature.hasStraightWalkingPathTo(target)
                                    && creature.horizontalDistanceFromEntity(target) >= 6D
                                    && creature.horizontalDistanceFromEntity(target) <= 16D) {
                                return 4;
                            }
                            return -1;
                        })
                        .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(6D, 16D))
                        .setDontPathToTarget()
        );

        CreatureMoveRegistry.registerMove("belch",
                new CreatureMoveBuilder()
                        .setStaminaCost(0.2f)
                        .setElement(Element.POISON)
                        .setMoveType(CreatureMoveBuilder.MoveType.SPECIAL)
                        .setBasePower(50)
                        .setRequireFindTargetToUse()
                        .setOnMoveBeginEffect((creature, target) -> {
                            AnimationDataEntityLiving data = creature.getAnimationData();
                            CreatureLookUtil.LookPose look = CreatureLookUtil.getAttackLookPose(creature);
                            data.setVariable("head_attack_pitch", look.pitch());
                            data.setVariable("head_attack_yaw", look.yaw());
                        })
                        .setUseLookBasedFacing()
                        .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.RANGED)
                        .setRiddenPitchBounds(-75f, 35f)
                        .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.POINT)
                        .setOnMoveHitEffect(creature -> {
                            ProjectileBuilder belchProjectile = new ProjectileBuilder().setName("belch")
                                    .setRotateAlongPitch().setUseCubeModel()
                                    .setProjectileTrail(ProjectileTrail.makeBuilderForParticleTrail()
                                            .addParticleComponent(ProjectileTrail.DUST_APPEARANCE.setSize(0.5D, 0.5D))
                                            .addParticleComponent(new AppearanceTintingComponent.Builder().setColor(0x3B5817, false))
                                    );

                            //when ridden, shoot forward
                            if (creature.getControllingPassenger() != null) {
                                creature.launchProjectile(belchProjectile, 3f, 0f);
                            }
                            //otherwise, hit target
                            else {
                                EntityLivingBase target = creature.getAttackTarget();
                                if (target == null || !target.isEntityAlive()) return;

                                creature.launchProjectile(belchProjectile, target, 3f, 0f);
                            }
                        }),
                new MoveRuleBuilder("belch")
                        .setPriorityPredicate((creature, target) -> {
                            return target != null && target.isEntityAlive() ? 0 : -1;
                        })
                        .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(6D, 16D))
                        .addDetectionRule(new VerticalDistanceFromUserDetectionRule(5D, 16D))
                        .setUseWhenFrustrated()
        );
        registered = true;
    }

    //-----helpers for moves-----
    /**
     * Keep bite's initial swing, then fit the body pitch to an unobstructed flame direction.
     */
    private static void captureFlamethrowerLook(@NotNull ICreature creature, @Nullable Entity target) {
        AnimationDataEntityLiving data = creature.getAnimationData();
        data.setVariable("flamethrower_body_aim", data.getHolder().isBeingRidden() ? 0D : 1D);
        data.setVariable(
                "flamethrower_aim_swing",
                MathUtil.slopeResult(
                        data.getHeadPitch(1f), true,
                        CreatureLookUtil.HEAD_PITCH_MIN, 0D, -1D, 1D
                )
        );
        data.setVariable("flamethrower_release_pitch", 20D * data.getVariable("flamethrower_aim_swing"));
        double windupPitch;
        if (data.getHeadPitch(1f) < 0f) {
            windupPitch = data.getVariable("flamethrower_release_pitch") + 30D;
        }
        else {
            windupPitch = data.getVariable("flamethrower_release_pitch") - 30D;
        }
        data.setVariable("flamethrower_windup_pitch", windupPitch);
        if (data.getHolder().isBeingRidden() || !(target instanceof EntityLivingBase livingTarget)
                || !livingTarget.isEntityAlive()
                || !(data.getHolder() instanceof IAnimatable<?> animatable)
                || !data.getBoneSnapshotCollection().containsKey("body")) return;
        AnimatedGeoModel<?> model = ServerModelRegistry.getServerModel(animatable);
        AnimatedLocator locator = data.getAnimatedLocator("flamethrowerAimLocator");
        if (model == null || locator == null) return;
        List<String> names = CreatureMoveUtil.getAnimationNames(creature, creature.getPhase(), "flamethrower", ChargeupPhase.RELEASING);
        if (names.isEmpty()) return;
        Animation animation = model.getAnimations(names.getFirst(), animatable);
        if (animation == null) return;

        boolean elevatedTarget = livingTarget.posY > data.getHolder().getPositionEyes(1f).y;
        double aimSwing = MathUtil.slopeResult(data.getHeadPitch(1f), true,
                CreatureLookUtil.HEAD_PITCH_MIN, CreatureLookUtil.HEAD_PITCH_MAX, -1D, 1D);
        if (elevatedTarget) aimSwing = -1D;
        data.setVariable("flamethrower_aim_swing", aimSwing);
        int bestAngle = 0;
        data.setVariable("flamethrower_release_pitch", -bestAngle);
        Map<String, BoneSnapshot> pose = AnimationPoseSampler.sample(animation, data, 0D);
        BoneSnapshot body = pose.get("body");
        float restingBodyPitch = data.getBoneSnapshotCollection().get("body").getLeft().getInitialSnapshot().getRotation().x;
        AxisAlignedBB targetBounds = livingTarget.getEntityBoundingBox();
        double closestDistance = Double.MAX_VALUE;
        for (int angle = -20; angle <= 60; angle++) {
            // RiftLibrary bone rotations use positive radians for the upward body tilt.
            body.getRotation().x = restingBodyPitch + (float) Math.toRadians(angle);
            RiftLibRay.RayPose flame = CreatureLookUtil.getRayPose(data, locator, pose);
            Vec3d origin = flame.origin();
            // Aim at the near side of the head so a tower cannot hide the chosen target point.
            Vec3d targetPosition = new Vec3d(Math.clamp(origin.x, targetBounds.minX, targetBounds.maxX),
                    livingTarget.getPositionEyes(1f).y, Math.clamp(origin.z, targetBounds.minZ, targetBounds.maxZ));
            if (data.getHolder().world.rayTraceBlocks(origin, targetPosition, false, true, false) != null) continue;
            Vec3d endpoint = origin.add(flame.direction().scale(FLAMETHROWER_MAX_MOTION_DISTANCE));
            RayTraceResult hit = targetBounds.calculateIntercept(origin, endpoint);
            // Stop at a body tilt whose locator direction reaches the target.
            if (hit != null && data.getHolder().world.rayTraceBlocks(origin, hit.hitVec, false, true, false) == null) {
                bestAngle = angle;
                break;
            }
            double alongFlame = Math.clamp(targetPosition.subtract(origin).dotProduct(flame.direction()),
                    0D, FLAMETHROWER_MAX_MOTION_DISTANCE);
            if (alongFlame <= 0D) continue;
            Vec3d closestPoint = origin.add(flame.direction().scale(alongFlame));
            double distance = closestPoint.squareDistanceTo(targetPosition);
            // Aim the locator toward the target, using only the configured reach and clear terrain.
            if (distance < closestDistance
                    && data.getHolder().world.rayTraceBlocks(origin, closestPoint, false, true, false) == null) {
                closestDistance = distance;
                bestAngle = angle;
            }
        }
        data.setVariable("flamethrower_release_pitch", -bestAngle);
        if (data.getHeadPitch(1f) < 0f || aimSwing < 0D) {
            windupPitch = Math.max(10D, -bestAngle + 30D);
        } else {
            windupPitch = -bestAngle - 30D;
        }
        data.setVariable("flamethrower_windup_pitch", windupPitch);
    }

    /**
     * Preserve the last visible neck pose and fit tail stab's downward strike to its target.
     *
     */
    private static void captureTailStabLook(@NotNull ICreature creature, @Nullable EntityLivingBase target) {
        AnimationDataEntityLiving data = creature.getAnimationData();
        CreatureLookUtil.LookPose look = CreatureLookUtil.getAttackLookPose(creature);
        data.setVariable("tail_attack_pitch", look.pitch());
        data.setVariable("tail_attack_yaw", look.yaw());
        data.setVariable("tail_attack_strike_pitch", -25.0991D - look.pitch());

        Vec3d targetPosition = getTailAttackTargetPosition(creature, target);
        if (targetPosition == null) return;
        TailStrikePose strike = createTailStrikePose(creature, "tail_stab", null, 12.5D);
        if (strike == null) return;
        if (target != null && target.isEntityAlive() && strike.crossesGroundTargetHeight(target)) return;

        Vec3d eyes = data.getHolder().getPositionEyes(1f);
        Vec3d offset = targetPosition.subtract(eyes);
        double distance = Math.sqrt(offset.x * offset.x + offset.z * offset.z);
        double pitch = Math.clamp(Math.toDegrees(Math.atan2(-offset.y, distance)), CreatureLookUtil.HEAD_PITCH_MIN, CreatureLookUtil.HEAD_PITCH_MAX);
        if (CreatureLookUtil.getRiddenAttackPosition(creature) != null) strike.fitPitchToHeight(targetPosition.y);
        else strike.fitPitchToHeight(eyes.y - Math.tan(Math.toRadians(pitch)) * distance);
    }

    /**
     * Prediction and windup completion share this capture; the release keeps these variables frozen.
     *
     */
    private static void captureThagomizeLook(@NotNull ICreature creature, @Nullable EntityLivingBase target) {
        AnimationDataEntityLiving data = creature.getAnimationData();
        CreatureLookUtil.LookPose look = CreatureLookUtil.getAttackLookPose(creature);
        if (!creature.getCurrentMove().isEmpty() && creature.getControllingPassenger() == null) {
            // The windup has its own authored pose; capture the physical head aim, not that pose.
            look = new CreatureLookUtil.LookPose(Math.clamp(data.getHeadPitch(1f), CreatureLookUtil.HEAD_PITCH_MIN, CreatureLookUtil.HEAD_PITCH_MAX), Math.clamp(data.getHeadYaw(1f), -15f, 15f));
        }
        data.setVariable("tail_attack_pitch", look.pitch());
        data.setVariable("tail_attack_yaw", look.yaw());
        data.setVariable("tail_attack_strike_pitch", -22.3499D - look.pitch());

        TailStrikePose strike = createTailStrikePose(creature, "thagomize", ChargeupPhase.RELEASING, 5D);
        if (strike == null) return;
        if (target != null && target.isEntityAlive() && Math.abs(look.pitch()) < 1E-3f && strike.crossesGroundTargetHeight(target))
            return;

        Vec3d eyes = data.getHolder().getPositionEyes(1f);
        Vec3d targetPosition = getTailAttackTargetPosition(creature, target);
        // AI keeps its head-based reach; riders supply an explicit strike height.
        double distance = 3D * data.getScale();
        if (targetPosition != null) {
            Vec3d offset = targetPosition.subtract(eyes);
            distance = Math.sqrt(offset.x * offset.x + offset.z * offset.z);
        }
        if (CreatureLookUtil.getRiddenAttackPosition(creature) != null && targetPosition != null)
            strike.fitPitchToHeight(targetPosition.y);
        else strike.fitPitchToHeight(eyes.y - Math.tan(Math.toRadians(look.pitch())) * distance);
    }

    @Nullable
    private static Vec3d getTailAttackTargetPosition(@NotNull ICreature creature, @Nullable EntityLivingBase target) {
        if (target != null && target.isEntityAlive()) return target.getEntityBoundingBox().getCenter();
        return CreatureLookUtil.getRiddenAttackPosition(creature);
    }

    @Nullable
    private static TailStrikePose createTailStrikePose(
            @NotNull ICreature creature, @NotNull String moveName,
            @Nullable ChargeupPhase phase, double tick
    ) {
        AnimationDataEntityLiving data = creature.getAnimationData();
        if (!(data.getHolder() instanceof IAnimatable<?> animatable)) return null;

        List<AnimatedBoundingBox> hitboxes = data.getAnimatedBoundingBoxesByTag().get("thagomizer");
        if (hitboxes == null || hitboxes.isEmpty() || data.getBoneSnapshotCollection().isEmpty()) return null;

        AnimatedGeoModel<?> model = ServerModelRegistry.getServerModel(animatable);
        if (model == null) return null;

        List<String> names = CreatureMoveUtil.getAnimationNames(creature, creature.getPhase(), moveName, phase);
        if (names.isEmpty()) return null;
        Animation animation = model.getAnimations(names.getFirst(), animatable);
        if (animation == null) return null;

        return new TailStrikePose(data, animation, hitboxes, tick);
    }

    /**
     * Geometry utilities shared by both attacks, with no move-selection logic.
     *
     */
    private record TailStrikePose(
            @NotNull AnimationDataEntityLiving data, @NotNull Animation animation,
            @NotNull List<AnimatedBoundingBox> hitboxes, double tick
    ) {
        private boolean crossesGroundTargetHeight(@NotNull EntityLivingBase target) {
            if (Math.abs(target.getEntityBoundingBox().minY - this.data.getHolder().getEntityBoundingBox().minY) > this.data.getHolder().stepHeight) {
                return false;
            }
            // Keep an authored downstroke if it already reaches a ground target, including short creatures.
            Map<String, BoneSnapshot> pose = AnimationPoseSampler.sample(this.animation, this.data, this.tick);
            AxisAlignedBB targetBounds = target.getEntityBoundingBox();
            return this.hitboxes.stream().anyMatch(hitbox -> {
                AxisAlignedBB bounds = hitbox.getWorldSpaceBounds(
                        this.data.getScale(), this.data.getModelYaw(),
                        this.data.getHolder().getPositionVector(), pose
                );
                return bounds.minY < targetBounds.maxY && bounds.maxY > targetBounds.minY;
            });
        }

        private void fitPitchToHeight(double targetHeight) {
            // Fit the tail joint itself; the body's turn makes copying head pitch insufficient.
            float lowerPitch = -60f;
            float upperPitch = 60f;
            double lowerHeight = this.sampleHeight(lowerPitch);
            double upperHeight = this.sampleHeight(upperPitch);
            targetHeight = Math.clamp(targetHeight, Math.min(lowerHeight, upperHeight), Math.max(lowerHeight, upperHeight));
            for (int step = 0; step < 10; step++) {
                float pitch = (lowerPitch + upperPitch) / 2f;
                double height = this.sampleHeight(pitch);
                if ((height < targetHeight) == (upperHeight > lowerHeight)) lowerPitch = pitch;
                else upperPitch = pitch;
            }
            this.data.setVariable("tail_attack_strike_pitch", (lowerPitch + upperPitch) / 2f);
        }

        private double sampleHeight(float pitch) {
            this.data.setVariable("tail_attack_strike_pitch", pitch);
            Map<String, BoneSnapshot> pose = AnimationPoseSampler.sample(this.animation, this.data, this.tick);
            return this.hitboxes.stream().mapToDouble(hitbox -> {
                AxisAlignedBB foundHitbox = hitbox.getWorldSpaceBounds(this.data.getScale(), this.data.getModelYaw(), this.data.getHolder().getPositionVector(), pose);
                return foundHitbox.getCenter().y;
            }).average().orElseThrow();
        }
    }
}