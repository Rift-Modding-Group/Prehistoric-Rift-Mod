package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.server.entity.creatureMoves.AllyCallUtil;

public class AllyCallBuilder extends CreatureMoveBuilder {
    public AllyCallBuilder() {
        this.setMoveType(MoveType.STATUS)
                .setStaminaCost(0.1f)
                .setElement(Element.NEUTRAL)
                .setCooldown(1800)
                .setCanUse(creature -> !creature.isTemporaryAlly() && (creature.isTamed() || creature.canDoHerding() && !creature.isInHerd()))
                .setOnMoveHitEffect(AllyCallUtil::callAllies);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("ally_call", MoveCategory.STATUS);
            this.setPriorityPredicate((creature, target) -> target != null && target.isEntityAlive())
                    .setDontPathToTarget();
        }
    }
}
