package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;

import java.util.function.BiFunction;

/**
 * Common stuff for use among creature moves
 * */
public class CreatureMoveCommon {
    //-----some common raisePriorityPredicate instances for use-----
    public static final BiFunction<RiftCreature, Entity, Integer> generalMeleePredicate = (creatureNew, possibleTarget) -> {
        if (possibleTarget == null) return -1;
        if (creatureNew.getDistance(possibleTarget) <= creatureNew.getCreatureType().getPhysicalReach())
            return 3;
        return -1;
    };
    public static final BiFunction<RiftCreature, Entity, Integer> generalRangedPredicate = (creatureNew, possibleTarget) -> {
        if (possibleTarget == null) return -1;
        if (creatureNew.getDistance(possibleTarget) > creatureNew.getCreatureType().getPhysicalReach()
                && creatureNew.getDistance(possibleTarget) <= 16) //that 16 is temporary, idk really what to do with it
            return 2;
        return -1;
    };

    //-----templates for common creature moves-----
    public static final CreatureMoveBuilder standardMeleeMove = new CreatureMoveBuilder()
            .setMakesContact()
            .setPhysical()
            .setRequireFindTargetToUse()
            .setCanUsePredicate(generalMeleePredicate)
            .setOnMoveHitEffect(creature -> {
                creature.attackEntityAsMob(creature.getAttackTarget());
            });
}
