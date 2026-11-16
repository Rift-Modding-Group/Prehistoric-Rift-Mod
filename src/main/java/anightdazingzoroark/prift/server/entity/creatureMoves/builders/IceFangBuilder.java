package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;

public class IceFangBuilder extends StandardMeleeBuilder {
    public IceFangBuilder() {
        this.setUseBlockBreak()
                .setBasePower(50)
                .setElement(Element.ELECTRIC)
                .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureHeadAttackLook(creature))
                .setUseLookBasedFacing()
                .setOnMoveHitEffect(creature -> creature.createOffenseHitboxesByTag("jaw", 4))
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("ice_fang", MoveCategory.MELEE);
            this.setPriorityPredicate((creature, target) -> target != null)
                    .addDetectionRule(new AnimatedHitboxDetectionRule("jaw", CreatureLookUtil::captureHeadAttackLook));
        }
    }
}
