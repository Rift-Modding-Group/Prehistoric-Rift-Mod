package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.DistanceFromUserDetectionRule;
import net.minecraft.entity.EntityLivingBase;

public class PeckBuilder extends StandardMeleeBuilder {
    private static DistanceFromUserDetectionRule peckRange = new DistanceFromUserDetectionRule(2D);

    public PeckBuilder() {
        this.setBasePower(40)
                .setUseLookBasedFacing()
                .setOnMoveHitEffect(creature -> {
                    EntityLivingBase target = creature.getAttackTarget();
                    if (!creature.getEntityWorld().isRemote && target != null && target.isEntityAlive()
                            && peckRange.targetWithinRange(creature, target)
                    ) {
                        creature.attackEntityAsMob(target);
                    }
                });
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("peck");
            this.setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                    .addDetectionRule(peckRange);
        }
    }
}
