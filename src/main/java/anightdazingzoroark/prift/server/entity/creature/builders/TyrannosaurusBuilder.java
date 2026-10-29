package anightdazingzoroark.prift.server.entity.creature.builders;

import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.VerticalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HorizontalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.BoundingBoxDetectionRule;
import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.api.creature.builder.*;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveCommon;
import anightdazingzoroark.prift.util.RiftUtil;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityLiving;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.model.AnimationPoseSampler;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import anightdazingzoroark.riftlib.ray.IRayCreator;
import anightdazingzoroark.riftlib.ray.RiftLibRay;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import anightdazingzoroark.riftlib.ray.RiftLibRayHelper;
import anightdazingzoroark.riftlib.ray.rayShape.impact.RiftLibRayEllipsoidImpactShape;
import anightdazingzoroark.riftlib.ray.rayShape.impact.RiftLibRaySphereImpactShape;
import anightdazingzoroark.riftlib.ray.rayShape.motion.RiftLibRayConeMotionShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Map;

public class TyrannosaurusBuilder extends RiftCreatureBuilder {
    private static final double FLAMETHROWER_MAX_MOTION_DISTANCE = 16D;

    public TyrannosaurusBuilder() {
        this.setName("tyrannosaurus")
                .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                .setCreatureDiet(RiftCreatureEnums.CreatureDiet.CARNIVORE)
                .setCreatureElement(Element.FIRE)
                .setTributeItemPartName("arm")
                .setStats(9, 7, 5, 7, 3)
                .setScaleRangeForAge(0.5f, 3.25f)
                .setSpawnEggColors(3670016, 2428687)
                .setMainHitboxSize(3.25f, 4f)
                .setMaxFallHeight(4)
                .setFallCreatesImpact()
                .setDaysUntilAdult(7)
                .setDomestication(new CreatureDomesticationBuilder()
                        .setTameFactor(0.33f)
                        .setTamingMethod(RiftCreatureEnums.TamingMethod.TRANQ_THEN_FEED)
                        .setLevelupRate(RiftCreatureEnums.LevelupRate.SLOW)
                        .setControllerRideLocator("controllerPos")
                )
                .setRetaliateWhenAttacked()
                .setFleeBehavior(
                        16f,
                        (creature, threat) -> {
                            return creature.getHealth() <= creature.getMaxHealth() * 0.15f
                                    && RiftUtil.entityInTargetGroup(threat, "human");
                        }
                )
                .setHitboxInformation(Map.of(
                        "leg", creature -> 0.25D,
                        "body", creature -> 0.75D,
                        "head", creature -> 1.25D,
                        "tail", creature -> 0.5D
                ))
                .setNavigation(new CreatureNavigationBuilder().setCanWalk()
                        .setCanLeap(4D, 0, 12D)
                )
                .addBlockBreakLevel("pickaxe", 2)
                .addBlockBreakLevel("shovel", 2)
                .addBlockBreakLevel("axe", 2)
                //---rays---
                .addUsableRay(
                        "footStompRay",
                        new RiftLibRayBuilder()
                                .setImpactOnly()
                                .setImpactShape(() -> new RiftLibRayEllipsoidImpactShape(1D, 0.2D, 1D).topOnly())
                                .setMaxMotionDistance(5D)
                                .setOnlyOneSegment()
                                .setMotionSpeed(1.5D),
                        (creature, rayOrigin, rayHitResult) -> {
                            for (Entity hitEntity : rayHitResult.hitEntities()) {
                                if (!(hitEntity instanceof EntityLivingBase hitEntityLivingBase)) continue;
                                creature.attackEntityAsMob(hitEntityLivingBase);
                            }
                        }
                )
                .addUsableRay(
                        "roarRay",
                        new RiftLibRayBuilder()
                                .setImpactOnly()
                                .setImpactShape(() -> new RiftLibRaySphereImpactShape().topOnly())
                                .setMaxMotionDistance(12D)
                                .setMotionSpeed(1.5D)
                                .setBlockBreakCheck((rayCreator, blockPos) -> {
                                    IBlockState blockState = rayCreator.getRayCreator().world.getBlockState(blockPos);
                                    float hardness = blockState.getBlockHardness(rayCreator.getRayCreator().world, blockPos);
                                    return hardness <= 1f && hardness >= 0f;
                                })
                                .setImpactCreationInterval(5),
                        (creature, rayOrigin, rayHitResult) -> {
                            for (Entity hitEntity : rayHitResult.hitEntities()) {
                                if (!(hitEntity instanceof EntityLivingBase hitEntityLivingBase)) continue;
                                creature.attackEntityAsMob(hitEntityLivingBase);
                            }
                        }
                )
                .addUsableRay(
                        "flamethrowerRay",
                        new RiftLibRayBuilder()
                                .setMotionOnly()
                                .setMovementShape(() -> new RiftLibRayConeMotionShape(
                                        0.1D, 0.35D, 0.25D,
                                        3, 12, false, false
                                ))
                                .setMotionSpeed(1D)
                                .setMaxMotionDistance(FLAMETHROWER_MAX_MOTION_DISTANCE)
                                .setBlockBreakCheck(((rayCreator, pos) -> {
                                    World world = rayCreator.getRayCreator().world;
                                    IBlockState blockState = world.getBlockState(pos);
                                    return blockState.getBlock().isFlammable(world, pos, EnumFacing.UP);
                                })),
                        (creature, rayOrigin, rayHitResult) -> {
                            for (Entity hitEntity : rayHitResult.hitEntities()) {
                                if (!(hitEntity instanceof EntityLivingBase hitEntityLivingBase)) continue;
                                creature.attackEntityAsMob(hitEntityLivingBase);
                            }
                            for (BlockPos hitBlockPos : rayHitResult.hitBlockPositions()) {
                                //allow for snow
                                World world = creature.getEntityWorld();
                                if (world.getBlockState(hitBlockPos).getBlock() == Blocks.SNOW_LAYER
                                        && Blocks.FIRE.canPlaceBlockAt(world, hitBlockPos)
                                        && creature.getRNG().nextInt(4) == 0) {
                                    world.setBlockState(hitBlockPos, Blocks.FIRE.getDefaultState(), 3);
                                }
                                //allow for well almost anything else
                                else if (!world.isAirBlock(hitBlockPos)
                                        && world.isAirBlock(hitBlockPos.up())
                                        && Blocks.FIRE.canPlaceBlockAt(world, hitBlockPos.up())
                                        && creature.getRNG().nextInt(4) == 0) {
                                    world.setBlockState(hitBlockPos.up(), Blocks.FIRE.getDefaultState(), 3);
                                }
                            }
                        }
                )
                //---moves---
                .addMove("bite", CreatureMoveCommon.standardMeleeMove.copy()
                        .setBasePower(50)
                        .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureHeadAttackLook(creature, "bite", "jaw", 4D))
                        .setUseLookBasedFacing()
                        .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                        .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE)
                        .setAnimNames("bite")
                )
                .addMove("stomp", new CreatureMoveBuilder()
                        .setStaminaCost(0.02f)
                        .setBasePower(30)
                        .setRequireFindTargetToUse()
                        .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                        .setOnMoveHitEffect(creature -> {
                            if (!(creature instanceof IRayCreator<?> rayCreator)) return;
                            RiftLibRayHelper.createRay(rayCreator, "footStompRay", "stompOrigin");
                        })
                        .setAnimNames("stomp")
                )
                .addMove("flame_spit", new CreatureMoveBuilder()
                        .setStaminaCost(0.15f)
                        .setBasePower(40)
                        .setRequireFindTargetToUse()
                        .setElement(Element.FIRE)
                        .setMoveType(CreatureMoveBuilder.MoveType.SPECIAL)
                        .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureHeadAttackLook(creature, "flame_spit", "flame_spit", 8.75D))
                        .setUseLookBasedFacing()
                        .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                        .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE)
                        .setCooldown(300)
                        .setAnimNames("flame_spit")
                )
                .addMove("power_roar", new CreatureMoveBuilder()
                        .setStaminaCost(0.15f)
                        .setBasePower(20)
                        .setRequireFindTargetToUse()
                        .setMoveType(CreatureMoveBuilder.MoveType.SPECIAL)
                        .setElement(Element.SONIC)
                        .setOnMoveHitEffect(creature -> {
                            if (!(creature instanceof IRayCreator<?> rayCreator)) return;
                            RiftLibRayHelper.createRay(rayCreator, "roarRay", "centerPoint");
                        })
                        .setOnMoveEndEffect(creature -> {
                            if (!(creature instanceof IRayCreator<?> rayCreator)) return;
                            RiftLibRayHelper.killRay(rayCreator, "roarRay");
                        })
                        .setAnimNames("power_roar")
                        .setCooldown(1200)
                )
                .addMove("flamethrower", new CreatureMoveBuilder()
                        .setStaminaCost(0.08f)
                        .setStaminaDrainPerSecond(0.0125f)
                        .setBasePower(90)
                        .setRequireFindTargetToUse()
                        .setMoveType(CreatureMoveBuilder.MoveType.SPECIAL)
                        .setElement(Element.FIRE)
                        .setOnMoveBeginEffect(this::captureFlamethrowerLook)
                        .setWhileMoveUseEffect((creature, target) -> {
                            if (!creature.getAnimationData().getHolder().isBeingRidden() && (target == null || !target.isEntityAlive())) return;
                            this.captureFlamethrowerLook(creature, target);
                        })
                        .setMoveChargeupBuilder(new CreatureMoveChargeupBuilder()
                                .setChargeUpWhileUse(true)
                                .setCanLookWhileUsing(true)
                                .setMaxChargeUp(300)
                                .setPrereleaseEndEffect(creature -> {
                                    RiftLibRayHelper.createRay(creature.asRayCreator(), "flamethrowerRay", "flamethrowerAimLocator");
                                })
                                .setReleaseEndEffect(creature -> {
                                    RiftLibRayHelper.killRay(creature.asRayCreator(), "flamethrowerRay");
                                })
                        )
                        .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.RANGED)
                        .setRiddenPitchBounds(-75f, 35f)
                        .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.POINT)
                )
                //---attack ai---
                .setMoveSelector(new CreatureMoveSelectorBuilder()
                                .setMoveRule(
                                        new MoveRuleBuilder("bite")
                                                .setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                                                .addDetectionRule(new AnimatedHitboxDetectionRule("jaw", creature -> CreatureLookUtil.captureHeadAttackLook(creature, "bite", "jaw", 4D)))
                                                .setUseBlockBreak()
                                )
                                .setMoveRule(
                                        new MoveRuleBuilder("stomp")
                                                .setPriorityPredicate((creature, target) -> {
                                                    return (target != null && target.isEntityAlive() && !creature.bodyTouchingLiquid()
                                                            && creature.isOnGround()
                                                            && creature.aabbIntersectsBoundingBox(target.getEntityBoundingBox(), "stompHitZone")) ?
                                                            0 : -1;
                                                })
                                                .addDetectionRule(new BoundingBoxDetectionRule("stompHitZone"))
                                                .setDontPathToTarget()
                                )
                                .setMoveRule(
                                        new MoveRuleBuilder("flame_spit")
                                                .setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                                                .addDetectionRule(new AnimatedHitboxDetectionRule("flame_spit", creature -> CreatureLookUtil.captureHeadAttackLook(creature, "flame_spit", "flame_spit", 8.75D)))
                                                .setUseBlockBreak()
                                )
                                .setMoveRule(
                                        new MoveRuleBuilder("power_roar")
                                                .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(12D))
                                                .setUseWhenFrustrated()
                                                .setDontPathToTarget()
                                )
                                .setMoveRule(
                                        new MoveRuleBuilder("flamethrower")
                                                .setPriorityPredicate((creature, target) -> {
                                                    return (target != null && target.isEntityAlive() && (creature.atRageThreshold() || creature.isUnableToPathToTarget())) ? 0 : -1;
                                                })
                                                .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(6D, 16D))
                                                .addDetectionRule(new VerticalDistanceFromUserDetectionRule(5D, 16D))
                                                .setUseWhenFrustrated()
                                )
                                .setCanSprintToAttack(1, 8D, 16D)
                                .setCanLeapToAttack(1, 6D, 12D, false)
                )
                //---targeting---
                .addDefaultTargetWhitelistEntry("animal")
                .addDefaultTargetWhitelistEntry("herbivoreCreature")
                .addDefaultFoodItemWhitelistEntry("meat")
                .setRetaliateWhenAttacked()
                .setRememberPlayerAttacker();
    }

    //-----helpers for moves-----
    /**
     * Keep bite's initial swing, then fit the body pitch to an unobstructed flame direction.
     * */
    private void captureFlamethrowerLook(@NotNull ICreature creature, @Nullable Entity target) {
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
        Animation animation = model.getAnimations("animation.tyrannosaurus.flamethrower_releasing", animatable);
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
        }
        else {
            windupPitch = -bestAngle - 30D;
        }
        data.setVariable("flamethrower_windup_pitch", windupPitch);
    }

}
