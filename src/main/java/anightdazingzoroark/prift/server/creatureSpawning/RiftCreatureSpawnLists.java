package anightdazingzoroark.prift.server.creatureSpawning;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.config.RiftCreatureConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RiftCreatureSpawnLists {
    public static List<BiomeSpawner> createSpawnerList(List<Biome> biomeList, World world, String category) {
        List<BiomeSpawner> biomeSpawners = new ArrayList<>();
        int timeOfDay = (int)(world.getWorldTime() % 24000L);
        for (Biome biomeToTest : biomeList) {
            List<Spawner> spawnerList = new ArrayList<>();
            for (RiftCreatureType creatureType : RiftCreatureType.values()) {
                for (RiftCreatureConfig.SpawnRule spawnRule: RiftConfigHandler.getConfig(creatureType).spawnRules) {
                    List<Biome> configBiomeList = getBiomeList(spawnRule);
                    if (configBiomeList.contains(biomeToTest) && timeOfDay >= spawnRule.timeRange.get(0) && timeOfDay <= spawnRule.timeRange.get(1) && spawnRule.category.equals(category)) {
                        spawnerList.add(new Spawner(spawnRule.weight, creatureType)
                                .setSpawnAmntRange(spawnRule.spawnAmntRange.get(0), spawnRule.spawnAmntRange.get(1))
                                .setDensityLimit(spawnRule.densityLimit)
                                .setSpawnBlocksWhitelist(spawnRule.spawnBlocksWhitelist)
                                .setMustSpawnInRain(spawnRule.inRain)
                                .setSpawnOnLand(spawnRule.onLand)
                                .setSpawnInWater(spawnRule.inWater)
                                .setSpawnInAir(spawnRule.inAir)
                                .setMustSeeSky(spawnRule.mustSeeSky)
                                .setYLevelRange(spawnRule.yLevelRange)
                                .setDimensionId(spawnRule.dimensionId)
                        );
                    }
                }
            }
            if (!spawnerList.isEmpty()) biomeSpawners.add(new BiomeSpawner(biomeToTest, spawnerList));
        }
        return biomeSpawners;
    }

    private static boolean spawnerListHasBiome(List<BiomeSpawner> biomeSpawnerList, Biome biome) {
        if (!biomeSpawnerList.isEmpty()) {
            for (BiomeSpawner biomeSpawner : biomeSpawnerList) {
                if (biomeSpawner.biome.equals(biome)) return true;
            }
        }
        return false;
    }

    private static List<Biome> getBiomeList(RiftCreatureConfig.SpawnRule spawnRule) {
        List<Biome> biomeList = new ArrayList<>();
        for (String entry : spawnRule.biomes) {
            int partOne = entry.indexOf(":");

            if (!entry.substring(0, 1).equals("-")) {
                String spawnerType = partOne != -1 ? entry.substring(0, partOne) : entry;

                if (spawnerType.equals("biome")) {
                    String biomeIdentifier = entry.substring(partOne + 1);
                    for (Biome biome : Biome.REGISTRY) {
                        if (biome.getRegistryName().toString().equals(biomeIdentifier)) {
                            biomeList.add(biome);
                        }
                    }
                }
                else if (spawnerType.equals("tag")) {
                    String biomeTag = entry.substring(partOne + 1);
                    for (Biome biome : Biome.REGISTRY) {
                        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.getType(biomeTag))) {
                            biomeList.add(biome);
                        }
                    }
                }
                else if (spawnerType.equals("all")) {
                    for (Biome biome : Biome.REGISTRY) {
                        biomeList.add(biome);
                    }
                }
            }
            else {
                String spawnerType = entry.substring(1, partOne);
                if (spawnerType.equals("biome")) {
                    String biomeIdentifier = entry.substring(partOne + 1);
                    for (Biome biome : Biome.REGISTRY) {
                        if (biome.getRegistryName().equals(biomeIdentifier)) {
                            biomeList.remove(biome);
                        }
                    }
                }
                else if (spawnerType.equals("tag")) {
                    String biomeTag = entry.substring(partOne + 1);
                    for (Biome biome : Biome.REGISTRY) {
                        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.getType(biomeTag))) {
                            biomeList.remove(biome);
                        }
                    }
                }
            }
        }
        return biomeList;
    }

    public static boolean biomeInDimension(Biome biome, int dimensionId) {
        WorldServer world = DimensionManager.getWorld(dimensionId);
        if (world != null) {
            WorldProvider provider = world.provider;
            BiomeProvider biomeProvider = provider.getBiomeProvider();
            return biomeProvider.getBiomesToSpawnIn().contains(biome);
        }
        return false;
    }

    public static class BiomeSpawner {
        public final Biome biome;
        public final List<Spawner> spawnerList;

        public BiomeSpawner(Biome biome, List<Spawner> spawnerList) {
            this.biome = biome;
            this.spawnerList = spawnerList;
        }

        public String toString() {
            return "{biome: "+biome.biomeName+", spawners: "+this.spawnerList+" }";
        }

        public Spawner getSpawnerRandomly() {
            int totalWeight = this.spawnerList.stream()
                    .mapToInt(Spawner::getWeight)
                    .sum();
            int randomValue = RiftUtil.randomInRange(0, totalWeight);
            for (Spawner spawnerToChoose : this.spawnerList) {
                randomValue -= spawnerToChoose.getWeight();
                if (randomValue <= 0) return spawnerToChoose;
            }
            return null;
        }
    }

    public static class Spawner {
        private final int weight;
        private final RiftCreatureType creatureType;
        private List<Integer> spawnAmntRange;
        private int densityLimit;
        private List<String> spawnBlocksWhitelist;
        private boolean inRain;
        private boolean onLand;
        private boolean inWater;
        private boolean inAir;
        private boolean mustSeeSky;
        private List<Integer> yLevelRange;
        private int dimensionId;

        public Spawner(int weight, RiftCreatureType creatureType) {
            this.weight = weight;
            this.creatureType = creatureType;
        }

        public String toString() {
            return "{weight: "+this.weight+", creaturetype: "+this.creatureType+" }";
        }

        public int getWeight() {
            return this.weight;
        }

        public RiftCreatureType getCreatureType() {
            return this.creatureType;
        }

        public int getSpawnAmnt() {
            return RiftUtil.randomInRange(this.spawnAmntRange.get(0), this.spawnAmntRange.get(1));
        }

        public int getDensityLimit() {
            return this.densityLimit;
        }

        public List<String> getSpawnBlocksWhitelist() {
            return this.spawnBlocksWhitelist;
        }

        public boolean getMustSpawnInRain() {
            return this.inRain;
        }

        public boolean getCanSpawnOnLand() {
            return this.onLand;
        }

        public boolean getCanSpawnInWater() {
            return this.inWater;
        }

        public boolean getCanSpawnInAir() {
            return this.inAir;
        }

        public boolean getMustSeeSky() {
            return this.mustSeeSky;
        }

        public List<Integer> getYLevelRange() {
            return this.yLevelRange;
        }

        public int getDimensionId() {
            return this.dimensionId;
        }

        public Spawner setSpawnAmntRange(int min, int max) {
            this.spawnAmntRange = Arrays.asList(min, max);
            return this;
        }

        public Spawner setDensityLimit(int value) {
            this.densityLimit = value;
            return this;
        }

        public Spawner setSpawnBlocksWhitelist(List<String> values) {
            this.spawnBlocksWhitelist = values;
            return this;
        }

        public Spawner setMustSpawnInRain(boolean value) {
            this.inRain = value;
            return this;
        }

        public Spawner setSpawnOnLand(boolean value) {
            this.onLand = value;
            return this;
        }

        public Spawner setSpawnInWater(boolean value) {
            this.inWater = value;
            return this;
        }

        public Spawner setSpawnInAir(boolean value) {
            this.inAir = value;
            return this;
        }

        public Spawner setMustSeeSky(boolean value) {
            this.mustSeeSky = value;
            return this;
        }

        public Spawner setYLevelRange(List<Integer> values) {
            this.yLevelRange = values;
            return this;
        }

        public Spawner setDimensionId(int value) {
            this.dimensionId = value;
            return this;
        }
    }
}
