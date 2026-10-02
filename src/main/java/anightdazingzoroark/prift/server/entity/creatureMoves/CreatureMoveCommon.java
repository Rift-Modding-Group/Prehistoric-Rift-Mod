package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;

/**
 * Common stuff for use among creature moves
 * */
public class CreatureMoveCommon {
    //-----templates for common creature moves-----
    public static final CreatureMoveBuilder standardMeleeMove = new CreatureMoveBuilder()
            .setMakesContact()
            .setPhysical()
            .setRequireFindTargetToUse()
            .setStaminaCost(0.02f)
            .setOnMoveHitEffect(creature -> {
                if (creature.getAttackTarget() != null) creature.attackEntityAsMob(creature.getAttackTarget());
            });
}
