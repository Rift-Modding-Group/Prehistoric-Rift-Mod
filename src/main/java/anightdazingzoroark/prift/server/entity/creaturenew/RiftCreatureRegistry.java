package anightdazingzoroark.prift.server.entity.creaturenew;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creatureMovesNew.CreatureMoveNew;
import anightdazingzoroark.prift.server.entity.creaturenew.builder.AbstractCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creaturenew.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creaturenew.info.RiftCreatureEnums;

import java.util.HashMap;

//this registers creatures
public class RiftCreatureRegistry {
    public static final HashMap<String, RiftCreatureBuilder> creatureBuilderMap = new HashMap<>();

    public static RiftCreatureBuilder getCreatureBuilder(String name) {
        return creatureBuilderMap.get(name);
    }

    public static void registerCreatureType(String name, RiftCreatureBuilder builder) {
        if (creatureBuilderMap.containsKey(name)) {
            RiftInitialize.logger.warn("Builder for creature type {} already exists!", name);
            return;
        }
        if (!builder.isValid()) {
            RiftInitialize.logger.warn("Builder for creature type {} is invalid!", name);
            return;
        }
        builder.setName(name);
        creatureBuilderMap.put(name, builder);
    }

    public static void createCreatures() {
        registerCreatureType(
                "tyrannosaurus",
                new RiftCreatureBuilder(TyrannosaurusNew.class)
                        .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                        .setCreatureDiet(RiftCreatureEnums.CreatureDiet.CARNIVORE)
                        .setStats(9, 7, 5, 7, 3)
                        .setScaleRangeForAge(0.5f, 3.25f)
                        .setSpawnEggColors(3670016, 2428687)
                        .setMainHitboxSize(3.25f, 4f)
                        .setDaysUntilAdult(7)
                        .setHostileToHumans()
                        .setRetaliateWhenAttacked()
                        .setPhysicalReach(5)
                        .setCanSprintToAttack()
                        .setLearnableMoves(
                                new CreatureMoveStorage.LearnableMoveHolder(CreatureMoveNew.BITE, "bite"),
                                new CreatureMoveStorage.LearnableMoveHolder(CreatureMoveNew.STOMP, "stomp")
                        )
                        .setInitMainUsableMoves(CreatureMoveNew.BITE, CreatureMoveNew.STOMP)
        );
        registerCreatureType(
                "stegosaurus",
                new RiftCreatureBuilder(StegosaurusNew.class)
                        .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                        .setCreatureDiet(RiftCreatureEnums.CreatureDiet.HERBIVORE)
                        .setStats(5.5, 5, 2, 4, 2)
                        .setScaleRangeForAge(0.3f, 2.125f)
                        .setSpawnEggColors(1731840, 16743424)
                        .setMainHitboxSize(2.125f, 2.5f)
                        .setDaysUntilAdult(3)
                        .setIsHerder()
                        .setRetaliateWhenAttacked(true)
                        .setPhysicalReach(5)
                        //.setCanSprintToAttack()
                        .setLearnableMoves(
                                new CreatureMoveStorage.LearnableMoveHolder(CreatureMoveNew.THAGOMIZE, "tail_attack")
                        )
                        .setInitMainUsableMoves(CreatureMoveNew.THAGOMIZE)
        );
        /*
        registerCreatureType(
                "dodo",
                new RiftCreatureBuilder().setCreatureCategory(RiftCreatureEnums.CreatureCategory.BIRD)
                        .setCreatureDiet(RiftCreatureEnums.CreatureDiet.HERBIVORE)
                        .setMovementOptions(RiftCreatureEnums.Movement.SLOW_FALL)
                        .setStats(20, 10, 5, 30)
                        .setScaleRangeForAge(0.5f, 0.75f)
                        .setSpawnEggColors(7828853, 6184028)
                        .setIsHerder()
        );
        registerCreatureType(
                "triceratops",
                new RiftCreatureBuilder().setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                        .setCreatureDiet(RiftCreatureEnums.CreatureDiet.HERBIVORE)
                        .setStats(120, 80, 30, 120)
                        .setScaleRangeForAge(0.3f, 1.75f)
                        .setSpawnEggColors(935177, 3631923)
                        .setDaysUntilAdult(3)
                        .setIsHerder()
                        .setRetaliateWhenAttacked(true)
        );
        registerCreatureType(
                "utahraptor",
                new RiftCreatureBuilder().setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                        .setCreatureDiet(RiftCreatureEnums.CreatureDiet.CARNIVORE)
                        .setStats(50, 40, 20, 60)
                        .setScaleRangeForAge(0.3f, 1f)
                        .setSpawnEggColors(5855577, 10439936)
                        .setDaysUntilAdult(3)
                        .setIsHerder()
                        .setHostileToHumans()
                        .setRetaliateWhenAttacked(true)
        );
         */
    }
}
