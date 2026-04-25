package anightdazingzoroark.prift.server.entity.creatureMovesNew;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creaturenew.RiftCreatureNew;
import net.minecraft.entity.Entity;

import java.util.HashMap;
import java.util.function.BiFunction;

public class CreatureMoveRegistry {
    private static final HashMap<String, CreatureMoveBuilder> moveBuilderMap = new HashMap<>();

    //some common raisePriorityPredicate instances for use
    private static final BiFunction<RiftCreatureNew, Entity, Integer> generalMeleePredicate = (creatureNew, possibleTarget) -> {
        if (possibleTarget == null) return -1;
        if (creatureNew.getDistance(possibleTarget) <= creatureNew.getCreatureType().getPhysicalReach())
            return 3;
        return -1;
    };
    private static final BiFunction<RiftCreatureNew, Entity, Integer> generalRangedPredicate = (creatureNew, possibleTarget) -> {
        if (possibleTarget == null) return -1;
        if (creatureNew.getDistance(possibleTarget) > creatureNew.getCreatureType().getPhysicalReach()
                && creatureNew.getDistance(possibleTarget) <= 16) //that 16 is temporary, idk really what to do with it
            return 2;
        return -1;
    };

    public static boolean moveExists(String name) {
        return moveBuilderMap.containsKey(name);
    }

    public static CreatureMoveBuilder getCreatureMove(String name) {
        return moveBuilderMap.get(name);
    }

    public static void registerMove(String name, CreatureMoveBuilder builder) {
        if (moveBuilderMap.containsKey(name)) {
            RiftInitialize.logger.warn("Builder for move {} already exists!", name);
            return;
        }
        if (!builder.isValid()) {
            RiftInitialize.logger.warn("Builder for move type {} is invalid!", name);
            return;
        }
        builder.setName(name);
        moveBuilderMap.put(name, builder);
    }

    public static void createMoves() {
        registerMove(CreatureMoveNew.BITE, new CreatureMoveBuilder()
                .setBasePower(50)
                .setMakesContact()
                .setRequireFindTargetToUse()
                .setPhysical()
                .setCanUsePredicate(generalMeleePredicate)
        );
        registerMove(CreatureMoveNew.THAGOMIZE, new CreatureMoveBuilder()
                .setBasePower(60)
                .setMakesContact()
                .setRequireFindTargetToUse()
                .setPhysical()
                .setCanUsePredicate(generalMeleePredicate)
        );
    }
}
