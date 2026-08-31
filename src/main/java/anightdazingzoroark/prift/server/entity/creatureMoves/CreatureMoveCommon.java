package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;

/**
 * Common stuff for use among creature moves
 * */
public class CreatureMoveCommon {
    //-----templates for common creature moves-----
    public static final CreatureMoveBuilder standardMeleeMove = new CreatureMoveBuilder()
            .setStaminaCost(0.02f)
            .setMakesContact()
            .setPhysical()
            .setRequireFindTargetToUse()
            .setOnMoveHitEffect(creature -> {
                creature.attackEntityAsMob(creature.getAttackTarget());
            });
}
