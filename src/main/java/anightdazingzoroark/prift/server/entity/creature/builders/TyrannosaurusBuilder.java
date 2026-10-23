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
import anightdazingzoroark.riftlib.ray.IRayCreator;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TyrannosaurusBuilder extends RiftCreatureBuilder {
    public TyrannosaurusBuilder() {
        this.setName("tyrannosaurus")
                .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                .setCreatureDiet(RiftCreatureEnums.CreatureDiet.CARNIVORE)
                .setTributeItemPartName("arm")
                .setStats(9, 7, 5, 7, 3)
                .setScaleRangeForAge(0.5f, 3.25f)
                .setSpawnEggColors(3670016, 2428687)
                .setMainHitboxSize(3.25f, 4f)
                .setMaxFallHeight(4)
                .setFallCreatesImpact()
                .setDaysUntilAdult(7)
                .setDomestication(new CreatureDomesticationBuilder()
                        .setTamingMethod(RiftCreatureEnums.TamingMethod.TRANQ_THEN_FEED)
                        .setLevelupRate(RiftCreatureEnums.LevelupRate.SLOW)
                        .setControllerRideLocator("controllerPos")
                )
                .setRetaliateWhenAttacked()
                .setCanRetreat()
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
                                .setMaxMotionDistance(16D)
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
                        .setOnMoveBeginEffect((creature, target) -> this.captureBiteLook(creature))
                        .setUseLookBasedFacing(true)
                        .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                        .setAnimNames("bite")
                )
                .addMove("stomp", new CreatureMoveBuilder()
                        .setStaminaCost(0.02f)
                        .setBasePower(30)
                        .setRequireFindTargetToUse()
                        .setPhysical()
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
                        .setElemental(Element.FIRE, 0)
                        .setCooldown(300)
                        .setAnimNames("flame_spit")
                )
                .addMove("power_roar", new CreatureMoveBuilder()
                        .setStaminaCost(0.15f)
                        .setBasePower(20)
                        .setRequireFindTargetToUse()
                        .setElemental(Element.NEUTRAL, 0)
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
                        .setElemental(Element.FIRE, 0)
                        .setWhileMoveUseEffect((creature, target) -> {
                            Vec3d aimPosition = creature.getRiddenAimPosition();
                            Vec3d targetPosition;
                            //assume mounted targeting and aiming
                            if (aimPosition != null) targetPosition = aimPosition;
                                //assume unmounted targeting
                            else {
                                if (target == null || !target.isEntityAlive()) return;
                                targetPosition = target.getPositionVector().add(0D, target.height / 2D, 0D);
                            }

                            //get distance between the locator and the target
                            Vec3d flameOrigin = creature.getLocatorWorldPos("flamethrowerAimLocator");
                            Vec3d targetOffset = targetPosition.subtract(flameOrigin);
                            double horizontalDistance = Math.sqrt(targetOffset.x * targetOffset.x + targetOffset.z * targetOffset.z);
                            if (horizontalDistance <= 1E-4D && Math.abs(targetOffset.y) <= 1E-4D) return;

                            //convert into angle using trig magic
                            double aimPitch = Math.toDegrees(Math.atan2(targetOffset.y, horizontalDistance));
                            double headBend = Math.clamp(aimPitch, -60D, 35D);
                            double aimPitchCorrection = headBend - aimPitch;

                            double targetYaw = Math.toDegrees(Math.atan2(-targetOffset.x, targetOffset.z));
                            Entity creatureEntity = creature.asRayCreator().getRayCreator();
                            double creatureYaw = creatureEntity.rotationYaw;
                            double aimYaw = MathHelper.wrapDegrees(targetYaw - creatureYaw);

                            //now set variable
                            creature.getAnimationData().setVariable("flamethrower_head_bend", headBend);
                            creature.getAnimationData().setVariable("flamethrower_aim_pitch", aimPitchCorrection);
                            creature.getAnimationData().setVariable("flamethrower_aim_yaw", Math.clamp(aimYaw, -80D, 80D));
                        })
                        .setMoveChargeupBuilder(new CreatureMoveChargeupBuilder()
                                .setChargeUpWhileUse(true)
                                .setMaxChargeUp(300)
                                .setPrereleaseEndEffect(creature -> {
                                    RiftLibRayHelper.createRay(creature.asRayCreator(), "flamethrowerRay", "flamethrowerAimLocator");
                                })
                                .setReleaseEndEffect(creature -> {
                                    RiftLibRayHelper.killRay(creature.asRayCreator(), "flamethrowerRay");
                                })
                        )
                        .setOnMoveEndEffect(creature -> {
                            creature.getAnimationData().setVariable("flamethrower_head_bend", 0);
                            creature.getAnimationData().setVariable("flamethrower_aim_pitch", 0);
                            creature.getAnimationData().setVariable("flamethrower_aim_yaw", 0);
                        })
                        .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.RANGED)
                )
                //---attack ai---
                .setMoveSelector(new CreatureMoveSelectorBuilder()
                                .setMoveRule(
                                        new MoveRuleBuilder("bite")
                                                .setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                                                .addDetectionRule(new AnimatedHitboxDetectionRule("jaw", this::captureBiteLook))
                                                .setUseBlockBreak()
                                )
                                /*
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
                                                .setPriorityPredicate((creature, target) -> target != null && !creature.bodyTouchingLiquid() ? 3 : -1)
                                                .addDetectionRule(new BoundingBoxDetectionRule("frontZone", true))
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
                                 */
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
     * Shared by reach prediction and move start so both use the same saved neck pose.
     * */
    private void captureBiteLook(@NotNull ICreature creature) {
        AnimationDataEntityLiving data = creature.getAnimationData();
        CreatureLookUtil.LookPose look = CreatureLookUtil.getLookPose(creature);
        data.setVariable("bite_pitch", look.pitch());
        data.setVariable("bite_yaw", look.yaw());
        data.setVariable("bite_swing", MathUtil.slopeResult(look.pitch(), true, -15D, 0D, -1D, 1D));
    }
}