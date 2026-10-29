package anightdazingzoroark.prift.server.entity.creature.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.api.creature.builder.*;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.BoundingBoxDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HorizontalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.util.RiftUtil;
import anightdazingzoroark.riftlib.ray.IRayCreator;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import anightdazingzoroark.riftlib.ray.RiftLibRayHelper;
import anightdazingzoroark.riftlib.ray.rayShape.impact.RiftLibRayEllipsoidImpactShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import java.util.Map;

public class TriceratopsBuilder extends RiftCreatureBuilder {
    public TriceratopsBuilder() {
        this.setName("triceratops")
                .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                .setCreatureDiet(RiftCreatureEnums.CreatureDiet.HERBIVORE)
                .setCreatureElement(Element.GUARD)
                .setTributeItemPartName("horn")
                .setStats(6.5D, 4, 2, 6, 1)
                .setScaleRangeForAge(0.3f, 1.75f)
                .setSpawnEggColors(935177, 3631923)
                .setMainHitboxSize(2f, 2f)
                .setMaxFallHeight(4)
                .setFallCreatesImpact()
                .setDaysUntilAdult(5)
                .setHitboxInformation(Map.of("head", creature -> 0D))
                .setNavigation(new CreatureNavigationBuilder().setCanWalk()
                        .setCanLeap(4D, 0, 12D)
                )
                .setSprintBasePower(60)
                .setSprintCooldown(100)
                .setDomestication(new CreatureDomesticationBuilder()
                        .setTamingMethod(RiftCreatureEnums.TamingMethod.TRANQ_THEN_FEED)
                        .setLevelupRate(RiftCreatureEnums.LevelupRate.NORMAL)
                        .setControllerRideLocator("controllerPos")
                )
                .setIsHerder(8)
                .setFleeBehavior(
                        16f,
                        (creature, threat) -> {
                            return creature.getHealth() <= creature.getMaxHealth() * 0.15f && !creature.isInHerd()
                                    && (RiftUtil.entityInTargetGroup(threat, "human")
                                    || RiftUtil.entityInTargetGroup(threat, "carnivoreCreature"));
                        }
                )
                .addBlockBreakLevel("axe", 2)
                .addBlockBreakLevel("pickaxe", 0)
                .addBlockBreakLevel("shovel", 0)
                //---rays---
                .addUsableRay(
                        "stompRay",
                        new RiftLibRayBuilder()
                                .setImpactOnly()
                                .setImpactShape(() -> new RiftLibRayEllipsoidImpactShape(1D, 0.2D, 1D).topOnly())
                                .setMaxMotionDistance(8D)
                                .setOnlyOneSegment()
                                .setMotionSpeed(1.5D),
                        (creature, rayOrigin, rayHitResult) -> {
                            for (Entity hitEntity : rayHitResult.hitEntities()) {
                                if (!(hitEntity instanceof EntityLivingBase hitEntityLivingBase)) continue;
                                creature.attackEntityAsMob(hitEntityLivingBase);
                            }
                        }
                )
                //---moves---
                .addMove("headbutt", new CreatureMoveBuilder()
                        .setMakesContact()
                        .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                        .setRequireFindTargetToUse()
                        .setStaminaCost(0.02f)
                        .setBasePower(50)
                        .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureHeadAttackLook(creature, "headbutt", "headAttack", 7.5D))
                        .setUseLookBasedFacing()
                        .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                        .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE)
                        .setAnimNames("headbutt")
                )
                .addMove("stomp", new CreatureMoveBuilder()
                        .setStaminaCost(0.02f)
                        .setBasePower(30)
                        .setRequireFindTargetToUse()
                        .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                        .setOnMoveHitEffect(creature -> {
                            if (!(creature instanceof IRayCreator<?> rayCreator)) return;
                            RiftLibRayHelper.createRay(rayCreator, "stompRay", "stompOrigin");
                        })
                        .setAnimNames("stomp")
                )
                .addMove("charge", new CreatureMoveBuilder()
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
                        )
                )
                /*
                .addMove("belch", new CreatureMoveBuilder()
                        .setStaminaCost(0.2f)
                        .setElement(Element.POISON)
                        .setMoveType(CreatureMoveBuilder.MoveType.SPECIAL)
                        .setBasePower(50)
                        .setRequireFindTargetToUse()
                        .setUseLookBasedFacing()
                        .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.RANGED)
                        .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.POINT)
                        .setCooldown(300)
                )
                 */
                //---attack ai---
                .setMoveSelector(new CreatureMoveSelectorBuilder()
                        .setMoveRule(
                                new MoveRuleBuilder("headbutt")
                                        .setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                                        .addDetectionRule(new AnimatedHitboxDetectionRule("headAttack", creature -> CreatureLookUtil.captureHeadAttackLook(creature, "headbutt", "headAttack", 7.5D)))
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
                        .setMoveRule(new MoveRuleBuilder("charge")
                                .setPriorityPredicate((creature, target) -> {
                                    return target != null && target.isEntityAlive() && creature.isOnGround()
                                            && !creature.bodyTouchingLiquid() && creature.hasStraightWalkingPathTo(target)
                                            && creature.horizontalDistanceFromEntity(target) >= 6D
                                            && creature.horizontalDistanceFromEntity(target) <= 16D ? 4 : -1;
                                })
                                .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(6D, 16D))
                                .setDontPathToTarget()
                        )
                        .setCanSprintToAttack(1, 2D, 16D)
                        .setCanLeapToAttack(1, 6D, 12D, false)
                )
                //---food---
                .addDefaultFoodItemWhitelistEntry("fruit")
                .addDefaultFoodItemWhitelistEntry("vegetable")
                .addDefaultFoodItemWhitelistEntry("grass")
                //---targeting---
                .setRetaliateWhenAttacked()
                .setRememberPlayerAttacker();
    }
}
