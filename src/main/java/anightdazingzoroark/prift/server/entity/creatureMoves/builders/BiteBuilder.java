package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;

public class BiteBuilder extends StandardMeleeBuilder {
    public BiteBuilder() {
        this.setBasePower(50)
                .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureHeadAttackLook(creature, "bite"))
                .setUseLookBasedFacing()
                .setOnMoveHitEffect(creature -> creature.createOffenseHitboxesByTag("jaw", 4))
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("bite");
            this.setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                    .addDetectionRule(new AnimatedHitboxDetectionRule("jaw", creature -> CreatureLookUtil.captureHeadAttackLook(creature, "bite")))
                    .setUseBlockBreak();
        }
    }
}
