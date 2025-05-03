package anightdazingzoroark.prift.server.creatureSpawning;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.config.RiftCreatureConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.MapGenStructureData;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.Loader;
import sereneseasons.api.season.SeasonHelper;

import java.util.*;
import java.util.stream.Collectors;

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
                        Spawner spawnerToAdd = new Spawner(spawnRule.weight, creatureType)
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
                                .setStructures(spawnRule.structures);
                        //serene seasons compat
                        if (Loader.isModLoaded(RiftInitialize.SERENE_SEASONS_MOD_ID)) spawnerToAdd = spawnerToAdd.setSeasons(spawnRule.seasons);
                        //gamestages compat
                        if (Loader.isModLoaded(RiftInitialize.GAME_STAGES_MOD_ID)) spawnerToAdd = spawnerToAdd.setGamestages(spawnRule.gamestages);
                        spawnerList.add(spawnerToAdd);
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

    public static class BiomeSpawner {
        public final Biome biome;
        public final List<Spawner> spawnerList;

        public BiomeSpawner(Biome biome, List<Spawner> spawnerList) {
            this.biome = biome;
            this.spawnerList = spawnerList;
        }

        public BiomeSpawner changeSpawnerBasedOnPos(World world, BlockPos pos) {
            List<Spawner> tempSpawnerList = this.spawnerList;

            //change spawnerList based on whether or not a creature can spawn in at a certain place
            //based on whether it can spawn on land, air, or in water
            tempSpawnerList = tempSpawnerList.stream().filter(
                    spawner -> {
                        boolean spawnAboveBlock = spawner.getCanSpawnOnLand() && this.canSpawnAboveBlock(world, pos, spawner.getSpawnBlocksWhitelist());
                        boolean spawnInWater = spawner.getCanSpawnInWater() && world.getBlockState(pos).getMaterial() == Material.WATER;
                        boolean spawnInAir = spawner.getCanSpawnInAir() && world.getBlockState(pos).getMaterial() == Material.AIR;
                        return spawnAboveBlock || spawnInWater || spawnInAir;
                    }
            ).collect(Collectors.toList());

            //change spawnerList based on dist from spawn and whether or not a creature can spawn there
            tempSpawnerList = tempSpawnerList.stream().filter(
                    spawner -> {
                        boolean isInDangerousMobsList = Arrays.asList(GeneralConfig.dangerousMobs).contains(spawner.creatureType.getIdentifier());
                        boolean isWithinDistance = RiftUtil.getDistNoHeight(pos, world.getSpawnPoint()) <= GeneralConfig.dangerSpawnPreventRadius;
                        boolean isBeforeStartSpawn = world.getTotalWorldTime() < 24000L * GeneralConfig.daysUntilDangerSpawnNearWSpawn;
                        return !isInDangerousMobsList || !isWithinDistance || !isBeforeStartSpawn || world.provider.getDimension() != 0;
                    }
            ).collect(Collectors.toList());

            //change spawnerList based on y position
            tempSpawnerList = tempSpawnerList.stream().filter(
                    spawner -> pos.getY() >= spawner.getYLevelRange().get(0) && pos.getY() <= spawner.getYLevelRange().get(1)
            ).collect(Collectors.toList());

            //change spawnerList based on dimension
            tempSpawnerList = tempSpawnerList.stream().filter(
                    spawner -> spawner.getDimensionId() == world.provider.getDimension()
            ).collect(Collectors.toList());

            //change spawnerList based on whether or not the position is below a block
            //note that glass and leaves are considered the same as air blocks
            tempSpawnerList = tempSpawnerList.stream().filter(
                    spawner -> {
                        boolean flag = true;
                        for (int y = pos.getY(); y <= 256; y++) {
                            if (flag) {
                                BlockPos newPos = new BlockPos(pos.getX(), y, pos.getZ());
                                flag = world.getBlockState(newPos).getMaterial() == Material.AIR || world.getBlockState(newPos).getMaterial() == Material.LEAVES || world.getBlockState(newPos).getMaterial() == Material.PLANTS || world.getBlockState(newPos).getMaterial() == Material.VINE || world.getBlockState(newPos).getMaterial() == Material.GLASS || world.getBlockState(newPos).getMaterial() == Material.WATER;
                            }
                            else break;
                        }
                        return flag == spawner.getMustSeeSky();
                    }
            ).collect(Collectors.toList());

            //change spawnerList based on whether or not its raining
            tempSpawnerList = tempSpawnerList.stream().filter(
                    spawner -> !spawner.getMustSpawnInRain() || world.isRaining()
            ).collect(Collectors.toList());

            //change spawnerList based on the structure associated with the pos
            tempSpawnerList = tempSpawnerList.stream().filter(
                    spawner -> {
                        if (spawner.structures == null || spawner.structures.isEmpty()) return true;

                        for (String structureString : spawner.structures) {
                            //get structure data
                            MapGenStructureData data = (MapGenStructureData) world.getPerWorldStorage().getOrLoadData(MapGenStructureData.class, structureString);

                            if (data == null) return false;

                            //parse structure data
                            List<ChunkPos> parsedStrucData = this.parseStructureData(data);

                            //get chunk data
                            ChunkPos chunkPos = new ChunkPos(pos);

                            if (parsedStrucData.contains(chunkPos)) return true;
                        }

                        return false;
                    }
            ).collect(Collectors.toList());

            //if serene seasons is installed, change spawnerList based on current season
            if (Loader.isModLoaded(RiftInitialize.SERENE_SEASONS_MOD_ID)) {
                tempSpawnerList = tempSpawnerList.stream().filter(
                        spawner -> {
                            if (spawner.seasons == null || spawner.seasons.isEmpty()) return true;
                            return spawner.seasons.contains(SeasonHelper.getSeasonState(world).getSubSeason().toString().toLowerCase());
                        }
                ).collect(Collectors.toList());
            }

            //if gamestages is installed, change spawnerList based on game stage
            if (Loader.isModLoaded(RiftInitialize.GAME_STAGES_MOD_ID)) {
                tempSpawnerList = tempSpawnerList.stream().filter(
                        spawner -> {
                            if (spawner.gamestages == null || spawner.gamestages.isEmpty()) return true;

                            //check if one player has a stage
                            for (EntityPlayer player : world.playerEntities) {
                                for (String stageString : spawner.gamestages) {
                                    if (GameStageHelper.hasStage(player, stageString)) return true;
                                }
                            }

                            return false;
                        }
                ).collect(Collectors.toList());
            }

            return new BiomeSpawner(this.biome, tempSpawnerList);
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

        private boolean canSpawnAboveBlock(World world, BlockPos pos, List<String> whitelist) {
            boolean flag = false;
            IBlockState blockState = world.getBlockState(pos.down());
            List<String> blockList = RiftUtil.uniteTwoLists(Arrays.asList(GeneralConfig.universalSpawnBlocks), whitelist);
            for (String blockString : blockList) {
                if (!flag) flag = RiftUtil.blockstateEqualToString(blockState, blockString);
            }
            return flag;
        }

        private List<ChunkPos> parseStructureData(MapGenStructureData data) {
            List<ChunkPos> chunks = new ArrayList<>();
            NBTTagCompound nbttagcompound = data.getTagCompound();

            for (String s : nbttagcompound.getKeySet()) {
                NBTBase nbtbase = nbttagcompound.getTag(s);

                if (nbtbase.getId() == 10) {
                    NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbtbase;

                    if (nbttagcompound1.hasKey("ChunkX") && nbttagcompound1.hasKey("ChunkZ")) {
                        int x = nbttagcompound1.getInteger("ChunkX");
                        int z = nbttagcompound1.getInteger("ChunkZ");
                        chunks.add(new ChunkPos(x, z));
                    }
                }
            }
            return chunks;
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
        private List<String> structures;
        private List<String> seasons;
        private List<String> gamestages;

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

        public Spawner setStructures(List<String> values) {
            this.structures = values;
            return this;
        }

        public Spawner setSeasons(List<String> values) {
            this.seasons = values;
            return this;
        }

        public Spawner setGamestages(List<String> values) {
            this.gamestages = values;
            return this;
        }
    }
}
