package anightdazingzoroark.prift.server.entity.creatureMoves;

/**
 * Common stuff for use among creature moves
 * */
public class CreatureMoveCommon {
    //-----templates for common creature moves-----
    public static final CreatureMoveBuilder standardMeleeMove = new CreatureMoveBuilder()
            .setMakesContact()
            .setPhysical()
            .setRequireFindTargetToUse()
            .setOnMoveHitEffect(creature -> {
                creature.attackEntityAsMob(creature.getAttackTarget());
            });
}
