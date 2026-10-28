package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.api.creature.builder.CreaturePhaseBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.server.entity.creature.builders.DodoBuilder;
import anightdazingzoroark.prift.server.entity.creature.builders.StegosaurusBuilder;
import anightdazingzoroark.prift.server.entity.creature.builders.TriceratopsBuilder;
import anightdazingzoroark.prift.server.entity.creature.builders.TyrannosaurusBuilder;
import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//this registers creatures
public class RiftCreatureRegistry {
    public static final String PLACEHOLDER_CREATURE = "placeholder";
    public static final String DEFAULT_CREATURE = "tyrannosaurus";

    //all creatures are stored here
    private static final LinkedHashMap<String, RiftCreatureBuilder> creatureBuilderMap = new LinkedHashMap<>();
    private static final LinkedHashMap<String, RiftCreatureBuilder> pendingAddonCreatures = new LinkedHashMap<>();
    private static boolean registeringBuiltInCreatures;
    private static boolean builtInCreaturesRegistered;
    private static boolean registrationFinished;

    public static RiftCreatureBuilder getCreatureBuilder(String name) {
        return creatureBuilderMap.get(name);
    }

    public static boolean hasCreatureBuilder(String name) {
        return creatureBuilderMap.containsKey(name);
    }

    public static List<String> getCreatureNames() {
        return List.copyOf(creatureBuilderMap.keySet());
    }

    public static Map<String, RiftCreatureBuilder> getCreatureBuilders() {
        return Collections.unmodifiableMap(creatureBuilderMap);
    }

    public static boolean creatureUsesHitboxes(String name) {
        RiftCreatureBuilder builder = creatureBuilderMap.get(name);
        return builder != null && builder.getHitboxInformation() != null;
    }

    public static RiftCreature createCreature(World world, String name) {
        String resolvedName = creatureBuilderMap.containsKey(name) ? name : PLACEHOLDER_CREATURE;
        if (creatureUsesHitboxes(resolvedName)) return new RiftCreatureHitboxed(world, resolvedName);
        return new RiftCreature(world, resolvedName);
    }

    public static void registerCreatureType(RiftCreatureBuilder builder) {
        String name = builder.getName();

        if (registrationFinished) {
            RiftInitialize.logger.warn("Creature type {} was registered after creature registration finished!", name);
            return;
        }
        if (!builtInCreaturesRegistered && !registeringBuiltInCreatures) {
            if (pendingAddonCreatures.putIfAbsent(name, builder) != null) {
                RiftInitialize.logger.warn("Builder for addon creature type {} already exists!", name);
            }
            return;
        }

        //-----verify creature builder first-----
        if (creatureBuilderMap.containsKey(name)) {
            RiftInitialize.logger.warn("Builder for creature type {} already exists!", name);
            return;
        }
        if (!builder.isValid()) {
            RiftInitialize.logger.warn("Builder for creature type {} is invalid!", name);
            return;
        }
        //-----verify creature move builders next-----
        for (ImmutablePair<String, CreatureMoveBuilder> moveEntry : builder.getMoves()) {
            if (!moveEntry.getValue().isValid()) {
                RiftInitialize.logger.warn("Move {} for creature type {} in phase '{}' is invalid!", moveEntry.getKey(), name, moveEntry.getKey());
                return;
            }
        }
        for (Map.Entry<String, CreaturePhaseBuilder> phaseEntry : builder.getPhaseBuilderMaps().entrySet()) {
            for (ImmutablePair<String, CreatureMoveBuilder> moveEntry: phaseEntry.getValue().getMoves()) {
                if (!moveEntry.getValue().isValid()) {
                    RiftInitialize.logger.warn("Move {} for creature type {} in phase '{}' is invalid!", moveEntry.getKey(), name, moveEntry.getKey());
                    return;
                }
            }
        }
        //-----finalize creature info and lock them-----
        builder.setName(name);
        builder.lock();
        creatureBuilderMap.put(name, builder);
    }

    //default creatures
    public static void createCreatures() {
        if (builtInCreaturesRegistered || registeringBuiltInCreatures) return;
        registeringBuiltInCreatures = true;

        registerCreatureType(new RiftCreatureBuilder()
                .setName(PLACEHOLDER_CREATURE)
                .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                .setCreatureDiet(RiftCreatureEnums.CreatureDiet.CARNIVORE)
                .setCreatureElement(Element.NEUTRAL)
                .setTributeItemPartName("stuffing")
                .setStats(1, 1, 1, 1, 1)
                .setScaleRangeForAge(1f, 1f)
                .setSpawnEggColors(0x4F7942, 0xD9C7A3)
                .setMainHitboxSize(1f, 1.375f)
                //registration requires a move, even for the harmless plush placeholder.
                .addMove("placeholder", new CreatureMoveBuilder()
                        .setMoveType(CreatureMoveBuilder.MoveType.STATUS)
                        .setAnimNames("placeholder")
                )
        );
        registerCreatureType(new TyrannosaurusBuilder());
        registerCreatureType(new StegosaurusBuilder());
        registerCreatureType(new DodoBuilder());
        registerCreatureType(new TriceratopsBuilder());
        /*
        registerCreatureType(
                "utahraptor",
                RiftCreature.class,
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

        registeringBuiltInCreatures = false;
        builtInCreaturesRegistered = true;
        for (Map.Entry<String, RiftCreatureBuilder> entry : pendingAddonCreatures.entrySet()) {
            registerCreatureType(entry.getValue());
        }
        pendingAddonCreatures.clear();
    }

    /**
     * Closes the registration window after addon registration listeners have run.
     */
    public static void finishCreatureRegistration() {
        if (!builtInCreaturesRegistered) {
            throw new IllegalStateException("Built-in creatures must be registered before addon registration can finish");
        }
        registrationFinished = true;
    }
}
