package anightdazingzoroark.prift.server.entity.creatureMovesNew;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creaturenew.builder.RiftCreatureBuilder;

import java.util.HashMap;

public class CreatureMoveRegistry {
    private static final HashMap<String, CreatureMoveBuilder> moveBuilderMap = new HashMap<>();

    public static boolean moveExists(String name) {
        return moveBuilderMap.containsKey(name);
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
                .setPhysical()
        );
        registerMove(CreatureMoveNew.THAGOMIZE, new CreatureMoveBuilder()
                .setBasePower(60)
                .setMakesContact()
                .setPhysical()
        );
    }
}
