package anightdazingzoroark.prift.server.config;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureRegistry;
import anightdazingzoroark.prift.server.entity.creature.builder.RiftCreatureBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * everything gson interpreting goes here
 * */
public class RiftJsonConfigParser {
    @NotNull
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @NotNull
    private final Path creatureConfigDirectory;
    @NotNull
    private final Path listsConfigPath;
    @NotNull
    private RiftListsConfig listsConfig;
    @NotNull
    private final Map<String, RiftCreatureConfig> creatureConfigs = new HashMap<>();

    public RiftJsonConfigParser(@NotNull Path minecraftConfigDirectory) {
        Path configDirectory = minecraftConfigDirectory.resolve(RiftInitialize.MODID);
        this.creatureConfigDirectory = configDirectory.resolve("creatures");
        this.listsConfigPath = configDirectory.resolve("lists.json");

        try {
            Files.createDirectories(configDirectory);
            Files.createDirectories(creatureConfigDirectory);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to create config directories", e);
        }

        this.loadLists();
        this.loadCreatureConfigs();
    }

    //---loading starts here---
    /**
     * Loads one RiftCreatureConfig for every creature type
     * currently registered in RiftCreatureRegistry.
     */
    public void loadCreatureConfigs() {
        this.creatureConfigs.clear();

        for (Map.Entry<String, RiftCreatureBuilder> builderEntry : RiftCreatureRegistry.creatureBuilderMap.entrySet()) {
            String creatureName = builderEntry.getKey();
            RiftCreatureBuilder builder = builderEntry.getValue();

            //define default riftcreatureconfig
            RiftCreatureConfig defaultValue = new RiftCreatureConfig();
            if (builder.hasDefaultTargetWhitelist()) defaultValue.targetWhitelist = builder.getDefaultTargetWhitelist();
            if (builder.hasDefaultTargetBlacklist()) defaultValue.targetBlacklist = builder.getDefaultTargetBlacklist();
            if (builder.hasDefaultFoodItemWhitelist()) defaultValue.foodItemWhitelist = builder.getDefaultFoodItemWhitelist();
            if (builder.hasDefaultFoodItemBlacklist()) defaultValue.foodItemBlacklist = builder.getDefaultFoodItemBlacklist();

            //now load and put in creature config map
            Path path = this.creatureConfigDirectory.resolve(creatureName + ".json");
            RiftCreatureConfig config = this.load(path, RiftCreatureConfig.class, defaultValue);
            this.creatureConfigs.put(creatureName, config);
        }
    }

    public void loadLists() {
        RiftListsConfig defaultList = new RiftListsConfig();

        //---define target groups first---
        defaultList.targetGroups.put(
                "animal",
                Arrays.asList(
                        "minecraft:sheep",
                        "minecraft:cow",
                        "minecraft:chicken",
                        "minecraft:pig",
                        "minecraft:horse",
                        "minecraft:rabbit",
                        "minecraft:wolf",
                        "minecraft:ocelot",
                        "minecraft:mule",
                        "minecraft:llama",
                        "minecraft:donkey",
                        "minecraft:parrot"
                )
        );
        defaultList.targetGroups.put(
                "human",
                Arrays.asList(
                        "minecraft:player",
                        "minecraft:villager",
                        "minecraft:witch",
                        "minecraft:evocation_illager",
                        "minecraft:vindication_illager",
                        "minecraft:illusioner"
                )
        );

        //---now food groups---
        defaultList.foodGroups.put(
                "meat",
                Arrays.asList(
                        new RiftCreatureFood.Builder("minecraft:beef")
                                .setPercentHealed(0.125f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:cooked_beef")
                                .setPercentHealed(0.25f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:chicken")
                                .setPercentHealed(0.125f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:cooked_chicken")
                                .setPercentHealed(0.25f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:porkchop")
                                .setPercentHealed(0.125f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:cooked_porkchop")
                                .setPercentHealed(0.25f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:mutton")
                                .setPercentHealed(0.125f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:cooked_mutton")
                                .setPercentHealed(0.25f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:rabbit")
                                .setPercentHealed(0.125f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:cooked_rabbit")
                                .setPercentHealed(0.25f)
                                .build()
                )
        );
        defaultList.foodGroups.put(
                "fish",
                Arrays.asList(
                        new RiftCreatureFood.Builder("minecraft:fish")
                                .setPercentHealed(0.1f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:cooked_fish")
                                .setPercentHealed(0.2f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:fish:1")
                                .setPercentHealed(0.1f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:cooked_fish:1")
                                .setPercentHealed(0.2f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:fish:2")
                                .setPercentHealed(0.1f)
                                .build()
                )
        );
        defaultList.foodGroups.put(
                "fruit",
                Arrays.asList(
                        new RiftCreatureFood.Builder("minecraft:apple")
                                .setPercentHealed(0.05f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:melon")
                                .setPercentHealed(0.05f)
                                .build()
                )
        );
        defaultList.foodGroups.put(
                "vegetable",
                Arrays.asList(
                        new RiftCreatureFood.Builder("minecraft:carrot")
                                .setPercentHealed(0.05f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:potato")
                                .setPercentHealed(0.05f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:baked_potato")
                                .setPercentHealed(0.1f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:poisonous_potato")
                                .setPercentHealed(0.05f)
                                .addFoodEffect("minecraft:poison", 5, 0)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:beetroot")
                                .setPercentHealed(0.05f)
                                .build()
                )
        );
        defaultList.foodGroups.put(
                "grass",
                Arrays.asList(
                        new RiftCreatureFood.Builder("minecraft:wheat")
                                .setPercentHealed(0.05f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:hay_block")
                                .setPercentHealed(0.2f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:tallgrass:1")
                                .setPercentHealed(0.05f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:double_plant:2")
                                .setPercentHealed(0.1f)
                                .build()
                )
        );
        defaultList.foodGroups.put(
                "leaves",
                Arrays.asList(
                        new RiftCreatureFood.Builder("minecraft:leaves")
                                .setPercentHealed(0.05f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:leaves:1")
                                .setPercentHealed(0.05f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:leaves:2")
                                .setPercentHealed(0.05f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:leaves:3")
                                .setPercentHealed(0.05f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:leaves2")
                                .setPercentHealed(0.05f)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:leaves2:1")
                                .setPercentHealed(0.05f)
                                .build()
                )
        );
        defaultList.foodGroups.put(
                "goldenApples",
                Arrays.asList(
                        new RiftCreatureFood.Builder("minecraft:golden_apple")
                                .setPercentHealed(0.125f)
                                .addFoodEffect("minecraft:absorption", 120, 0)
                                .addFoodEffect("minecraft:regeneration", 5, 1)
                                .build(),
                        new RiftCreatureFood.Builder("minecraft:golden_apple:1")
                                .setPercentHealed(0.125f)
                                .addFoodEffect("minecraft:absorption", 120, 3)
                                .addFoodEffect("minecraft:regeneration", 20, 1)
                                .addFoodEffect("minecraft:fire_resistance", 300, 0)
                                .addFoodEffect("minecraft:resistance", 300, 0)
                                .build()
                )
        );

        //now set
        this.listsConfig = load(this.listsConfigPath, RiftListsConfig.class, defaultList);
    }

    /**
     * le main load function
     * */
    private <T> T load(@NotNull Path path, @NotNull Class<T> type, @NotNull T defaultValue) {
        if (Files.notExists(path)) {
            save(path, defaultValue);
            return defaultValue;
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            T value = this.gson.fromJson(reader, type);

            if (value == null) {
                save(path, defaultValue);
                return defaultValue;
            }

            return value;
        }
        catch (IOException | JsonParseException e) {
            RiftInitialize.logger.error("Failed to load config {}", path, e);
            return defaultValue;
        }
    }
    //---loading ends here---

    //---saving starts here---
    public void saveCreatureConfig(@NotNull String creatureName) {
        RiftCreatureConfig config = this.creatureConfigs.get(creatureName);

        if (config == null) {
            RiftInitialize.logger.warn("Tried to save config for unknown creature type {}", creatureName);
            return;
        }

        this.save(this.creatureConfigDirectory.resolve(creatureName + ".json"), config);
    }

    public void saveLists() {
        this.save(this.listsConfigPath, this.listsConfig);
    }

    private void save(@NotNull Path path, @NotNull Object value) {
        try {
            Path parent = path.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                this.gson.toJson(value, writer);
            }
        }
        catch (IOException e) {
            RiftInitialize.logger.error("Failed to save config {}", path, e);
        }
    }
    //---saving ends here---

    //---getters from here on out---
    public RiftCreatureConfig getCreatureConfig(@NotNull String creatureName) {
        return this.creatureConfigs.get(creatureName);
    }

    @NonNull
    public RiftListsConfig getListsConfig() {
        return this.listsConfig;
    }
}
