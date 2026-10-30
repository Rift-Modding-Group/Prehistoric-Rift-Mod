package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.server.entity.creatureMoves.TailAttackLookUtil;

public class TailStabBuilder extends StandardMeleeBuilder {
    public TailStabBuilder() {
        this.setBasePower(50)
                .setOnMoveBeginEffect(TailAttackLookUtil::captureTailStabLook)
                .setOnMoveHitEffect(creature -> creature.createOffenseHitboxesByTag("thagomizer", 4))
                .setUseLookBasedFacing()
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("tail_stab");
            this.setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                    .addDetectionRule(new AnimatedHitboxDetectionRule("thagomizer", TailAttackLookUtil::captureTailStabLook))
                    .setUseBlockBreak();
        }
    }
}
