package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HitboxDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.server.entity.RiftMagmaStorm;

public class MagmaStormBuilder extends CreatureMoveBuilder {
    public MagmaStormBuilder() {
        this.setStaminaCost(0.3f)
                .setBasePower(120)
                .setRequireFindTargetToUse()
                .setElement(Element.FIRE)
                .setMoveType(CreatureMoveBuilder.MoveType.SPECIAL)
                .setOnMoveHitEffect(RiftMagmaStorm::spawnPair)
                .setUseLookBasedFacing()
                .setCooldown(600);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("magma_storm", MoveCategory.RANGED);
            this.setPriorityPredicate((creature, target) -> target != null && target.isEntityAlive())
                    .addDetectionRule(new HitboxDistanceFromUserDetectionRule(4D))
                    .setDontPathToTarget();
        }
    }
}
