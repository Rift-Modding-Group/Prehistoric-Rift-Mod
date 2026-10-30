package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.BoundingBoxDetectionRule;

public class TailSlamBuilder extends CreatureMoveBuilder {
    public TailSlamBuilder() {
        this.setMakesContact()
                .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                .setRequireFindTargetToUse()
                .setOnMoveHitEffect(creature -> creature.createOffenseHitboxesByTag("frontZone", 4))
                .setUseLookBasedFacing()
                .setStaminaCost(0.15f)
                .setBasePower(70)
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE)
                .setCooldown(400);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("tail_slam");
            this.setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                    .addDetectionRule(new BoundingBoxDetectionRule("frontZone", true))
                    .setUseBlockBreak();
        }
    }
}
