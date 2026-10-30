package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HorizontalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.util.MathUtil;

public class FlameChargeBuilder extends CreatureMoveBuilder {
    public FlameChargeBuilder() {
        this.setMakesContact()
                .setElement(Element.FIRE)
                .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                .setRequireFindTargetToUse()
                .setStaminaCost(0.05f)
                .setBasePower(50)
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.RANGED)
                .setRiddenPitchBounds(0f, 0f)
                .setOnMoveBeginEffect((creature, target) -> creature.prepareRush(4D, 10))
                .setMoveChargeupBuilder(new CreatureMoveChargeupBuilder()
                        .setChargeUpThenRelease()
                        .setCanRotateWhileCharging(true)
                        .setMaxChargeUp(100)
                        .setCooldownMultiplier(4D)
                        .setBasePowerMultiplier((creature, basePower, chargeUp) -> {
                            return MathUtil.slopeResult(chargeUp, false, 0, 100, 1D, 2D);
                        })
                        .setOnHitEntityDuringRelease((creature, chargeUp, target) -> {
                            int burnChance = creature.getEntityWorld().rand.nextInt(3);
                            if (burnChance == 0) target.setFire(5);
                        })
                        .setWindupEndEffect((creature, charge) -> {
                            int maximumRushTicks = (int) Math.ceil(MathUtil.slopeResult(charge, true, 0, 100, 0, 100));
                            creature.prepareRush(4D, maximumRushTicks);
                        })
                );
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("flame_charge");
            this.setPriorityPredicate((creature, target) -> {
                        if (target != null && target.isEntityAlive() && creature.isOnGround()
                                && !creature.bodyTouchingLiquid() && creature.hasStraightWalkingPathTo(target)
                                && creature.horizontalDistanceFromEntity(target) >= 6D
                                && creature.horizontalDistanceFromEntity(target) <= 16D
                        ) {
                            return 4;
                        }
                        return -1;
                    })
                    .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(6D, 16D))
                    .setDontPathToTarget();
        }
    }
}
