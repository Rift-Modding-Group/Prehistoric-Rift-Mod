package anightdazingzoroark.prift.server.entity.creaturenew;

import anightdazingzoroark.prift.server.entity.creaturenew.info.RiftCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creaturenew.info.RiftCreatureEnums;

import java.util.HashMap;

//this registers creatures
public class RiftCreatureRegistry {
    public static final HashMap<String, RiftCreatureBuilder> creatureBuilderMap = new HashMap<>();

    public static RiftCreatureBuilder getCreatureBuilder(String name) {
        return creatureBuilderMap.get(name);
    }

    public static void registerCreatureType(String name, RiftCreatureBuilder builder) {
        creatureBuilderMap.put(name, builder.setName(name));
    }

    public static void createCreatures() {
        registerCreatureType(
                "tyrannosaurus",
                new RiftCreatureBuilder(TyrannosaurusNew.class)
                        .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                        .setCreatureDiet(RiftCreatureEnums.CreatureDiet.CARNIVORE)
                        .setStats(9, 7, 5, 7)
                        .setScaleRangeForAge(0.5f, 3.25f)
                        .setSpawnEggColors(3670016, 2428687)
                        .setDaysUntilAdult(7)
                        .setHostileToHumans()
                        .setRetaliateWhenAttacked()
        );
        registerCreatureType(
                "stegosaurus",
                new RiftCreatureBuilder(StegosaurusNew.class)
                        .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                        .setCreatureDiet(RiftCreatureEnums.CreatureDiet.HERBIVORE)
                        .setStats(5.5, 5, 2, 4)
                        .setScaleRangeForAge(0.3f, 2.125f)
                        .setSpawnEggColors(1731840, 16743424)
                        .setDaysUntilAdult(3)
                        .setIsHerder()
                        .setRetaliateWhenAttacked(true)
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
