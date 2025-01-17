package anightdazingzoroark.prift.server.creatureSpawning;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.google.common.base.Predicate;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mod.EventBusSubscriber
public class RiftCreatureSpawning {
    private static final ExecutorService chunkLoadExecutor = Executors.newSingleThreadExecutor();

    //spawn creatures while player is in loaded chunks
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.isRemote) return;

        WorldServer world = (WorldServer) event.world;

        if (!world.getGameRules().getBoolean("doMobSpawning")) return;

        if (event.phase == TickEvent.Phase.END && world.getTotalWorldTime() % (long)GeneralConfig.spawnInterval == 0) {
            ExecutorService executor = Executors.newSingleThreadExecutor();

            executor.submit(() -> {
                try {
                    List<BlockPos> spawnPositions = new ArrayList<>();
                    ChunkProviderServer chunkProvider = world.getChunkProvider();

                    //add positions
                    for (Map.Entry<Long, Chunk> entry : chunkProvider.loadedChunks.entrySet()) {
                        for (int x = 0; x < 10; x++) {
                            Chunk chunk = entry.getValue();
                            spawnPositions.add(this.getRandomChunkPosition(world, chunk.x, chunk.z));
                        }
                    }

                    //remove positions too close to players
                    List<BlockPos> playerPositions = new ArrayList<>();
                    for (EntityPlayer player : world.playerEntities) playerPositions.add(player.getPosition());

                    Iterator<BlockPos> iterator = spawnPositions.iterator();
                    while (iterator.hasNext()) {
                        BlockPos pos = iterator.next();
                        boolean tooClose = playerPositions.stream().anyMatch(refPos -> {
                            double minDist = GeneralConfig.spawnAroundPlayerRad;
                            double maxDistance = Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16;
                            return pos.distanceSq(refPos) <= Math.pow(minDist, 2) || pos.distanceSq(refPos) >= Math.pow(maxDistance, 2);
                        });
                        if (tooClose) iterator.remove();
                    }

                    //first get list of biomes of all loaded chunks
                    List<Biome> biomeList = new ArrayList<>();
                    for (BlockPos pos : spawnPositions) {
                        Biome biome = world.getBiome(pos);
                        if (!biomeList.contains(biome)) biomeList.add(biome);
                    }
                    //generate
                    List<RiftCreatureSpawnLists.BiomeSpawner> landBiomeSpawnerList = RiftCreatureSpawnLists.createSpawnerList(biomeList, world, "LAND");
                    List<RiftCreatureSpawnLists.BiomeSpawner> waterBiomeSpawnerList = RiftCreatureSpawnLists.createSpawnerList(biomeList, world, "WATER");
                    List<RiftCreatureSpawnLists.BiomeSpawner> airBiomeSpawnerList = RiftCreatureSpawnLists.createSpawnerList(biomeList, world, "AIR");
                    List<RiftCreatureSpawnLists.BiomeSpawner> caveBiomeSpawnerList = RiftCreatureSpawnLists.createSpawnerList(biomeList, world, "CAVE");

                    //finally spawn the creatures based on spawnPositions and biomeSpawnerList
                    /*
                    if (this.spawnFromBiomeSpawnerLists(world, landBiomeSpawnerList, spawnPositions) ||
                            this.spawnFromBiomeSpawnerLists(world, waterBiomeSpawnerList, spawnPositions) ||
                            this.spawnFromBiomeSpawnerLists(world, airBiomeSpawnerList, spawnPositions) ||
                            this.spawnFromBiomeSpawnerLists(world, caveBiomeSpawnerList, spawnPositions)
                    ) RiftInitialize.logger.info("Creatures successfully spawned in");
                    */
                    this.spawnEntityOnMainThread(world, landBiomeSpawnerList, spawnPositions);
                    this.spawnEntityOnMainThread(world, waterBiomeSpawnerList, spawnPositions);
                    this.spawnEntityOnMainThread(world, airBiomeSpawnerList, spawnPositions);
                    this.spawnEntityOnMainThread(world, caveBiomeSpawnerList, spawnPositions);
                }
                catch (Exception e) {
                    RiftInitialize.logger.error("Error during spawn calculation: ", e);
                }
                finally {
                    executor.shutdown();
                }
            });
        }
    }

    //spawn creature for every new chunk player loads
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldLoad(ChunkEvent.Load event) {
        if (event.getWorld().isRemote) return;

        WorldServer world = (WorldServer) event.getWorld();

        if (world == null) return;

        if (!world.getGameRules().getBoolean("doMobSpawning")) return;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                List<BlockPos> spawnPositions = new ArrayList<>();
                for (int x = 0; x < 10; x++) spawnPositions.add(this.getRandomChunkPosition(world, event.getChunk().x, event.getChunk().z));

                //remove positions too close to players
                List<BlockPos> playerPositions = new ArrayList<>();
                for (EntityPlayer player : world.playerEntities) playerPositions.add(player.getPosition());

                Iterator<BlockPos> iterator = spawnPositions.iterator();
                while (iterator.hasNext()) {
                    BlockPos pos = iterator.next();
                    boolean tooClose = playerPositions.stream().anyMatch(refPos -> pos.distanceSq(refPos) <= Math.pow(GeneralConfig.spawnAroundPlayerRad, 2));
                    if (tooClose) iterator.remove();
                }

                //first get list of biomes of all loaded chunks
                List<Biome> biomeList = new ArrayList<>();
                for (BlockPos pos : spawnPositions) {
                    Biome biome = world.getBiome(pos);
                    if (!biomeList.contains(biome)) biomeList.add(biome);
                }

                //generate
                List<RiftCreatureSpawnLists.BiomeSpawner> landBiomeSpawnerList = RiftCreatureSpawnLists.createSpawnerList(biomeList, world, "LAND");
                List<RiftCreatureSpawnLists.BiomeSpawner> waterBiomeSpawnerList = RiftCreatureSpawnLists.createSpawnerList(biomeList, world, "WATER");
                List<RiftCreatureSpawnLists.BiomeSpawner> airBiomeSpawnerList = RiftCreatureSpawnLists.createSpawnerList(biomeList, world, "AIR");
                List<RiftCreatureSpawnLists.BiomeSpawner> caveBiomeSpawnerList = RiftCreatureSpawnLists.createSpawnerList(biomeList, world, "CAVE");

                //finally spawn the creatures based on spawnPositions and biomeSpawnerList
                /*
                if (this.spawnFromBiomeSpawnerLists(world, landBiomeSpawnerList, spawnPositions) ||
                        this.spawnFromBiomeSpawnerLists(world, waterBiomeSpawnerList, spawnPositions) ||
                        this.spawnFromBiomeSpawnerLists(world, airBiomeSpawnerList, spawnPositions) ||
                        this.spawnFromBiomeSpawnerLists(world, caveBiomeSpawnerList, spawnPositions)
                ) RiftInitialize.logger.info("Creatures successfully generated");
                */
                this.spawnEntityOnMainThread(world, landBiomeSpawnerList, spawnPositions);
                this.spawnEntityOnMainThread(world, waterBiomeSpawnerList, spawnPositions);
                this.spawnEntityOnMainThread(world, airBiomeSpawnerList, spawnPositions);
                this.spawnEntityOnMainThread(world, caveBiomeSpawnerList, spawnPositions);
            }
            catch (Exception e) {
                RiftInitialize.logger.error("Error during spawn calculation: ", e);
            }
            finally {
                executor.shutdown();
            }
        });
    }

    private void spawnEntityOnMainThread(World world, List<RiftCreatureSpawnLists.BiomeSpawner> biomeSpawners, List<BlockPos> spawnPositions) {
        MinecraftServer server = world.getMinecraftServer();
        if (server == null) return;

        server.addScheduledTask(() -> {
            for (BlockPos pos : spawnPositions) {
                for (RiftCreatureSpawnLists.BiomeSpawner biomeSpawner : biomeSpawners) {
                    if (world.getBiome(pos).equals(biomeSpawner.biome)) {
                        //make biome spawner based on position
                        RiftCreatureSpawnLists.BiomeSpawner newBiomeSpawner = biomeSpawner.changeSpawnerBasedOnPos(world, pos);
                        if (newBiomeSpawner.spawnerList.isEmpty()) continue;
                        RiftCreatureSpawnLists.Spawner spawnerToSpawn = newBiomeSpawner.getSpawnerRandomly();

                        //now spawn a creature
                        int spawnAmnt = spawnerToSpawn.getSpawnAmnt();
                        for (int x = 0; x < spawnAmnt; x++) {
                            //create creature
                            RiftCreature creatureToSpawn = spawnerToSpawn.getCreatureType().invokeClass(world);
                            creatureToSpawn.setPosition(pos.getX(), pos.getY(), pos.getZ());
                            creatureToSpawn.onInitialSpawn(world.getDifficultyForLocation(pos), null);

                            if (this.canFitInArea(creatureToSpawn, spawnerToSpawn)
                                    && this.otherCreaturesFarAway(creatureToSpawn)
                                    && this.testOtherCreatures(creatureToSpawn, spawnerToSpawn.getDensityLimit())
                                    && this.testForDangerNearSpawn(creatureToSpawn)
                            ) {
                                world.spawnEntity(creatureToSpawn);
                                if (GeneralConfig.spawnCreatureNotify) RiftInitialize.logger.info("Spawned "+creatureToSpawn.getName()+" at x=: "+pos.getX()+", y="+pos.getY()+", z="+pos.getZ());
                            }
                        }
                    }
                }
            }
        });
    }

    private BlockPos getRandomChunkPosition(World worldIn, int x, int z) {
        Chunk chunk = worldIn.getChunk(x, z);
        int i = x * 16 + worldIn.rand.nextInt(16);
        int j = z * 16 + worldIn.rand.nextInt(16);
        int k = MathHelper.roundUp(chunk.getHeight(new BlockPos(i, 0, j)) + 1, 16);
        int l = worldIn.rand.nextInt(k > 0 ? k : chunk.getTopFilledSegment() + 16 - 1);
        return new BlockPos(i, l, j);
    }

    //other spawn conditions
    private boolean testForDangerNearSpawn(RiftCreature creature) {
        if (!Arrays.asList(GeneralConfig.dangerousMobs).contains(EntityList.getKey(creature).toString())) return true;
        boolean nearSpawn = RiftUtil.getDistNoHeight(creature.getPosition(), creature.world.getSpawnPoint()) <= GeneralConfig.dangerSpawnPreventRadius;
        boolean cannotStart = creature.world.getTotalWorldTime() < 24000L * GeneralConfig.daysUntilDangerSpawnNearWSpawn;
        return !nearSpawn || !cannotStart || creature.world.provider.getDimension() != 0;
    }

    private boolean otherCreaturesFarAway(RiftCreature creature) {
        List<RiftCreature> creatureList = creature.world.getEntitiesWithinAABB(RiftCreature.class, creature.getEntityBoundingBox().grow(24D), new Predicate<RiftCreature>() {
            @Override
            public boolean apply(@Nullable RiftCreature creatureTest) {
                return !creatureTest.isTamed() && !creatureTest.equals(creature) && !creatureTest.getClass().equals(creature.getClass());
            }
        });
        return creatureList.isEmpty();
    }

    private boolean canFitInArea(RiftCreature creature, RiftCreatureSpawnLists.Spawner spawner) {
        int xMin = (int)Math.floor(creature.width / 2);
        for (int x = -xMin; x <= xMin; x++) {
            for (int y = 0; y < (int)Math.ceil(creature.height); y++) {
                for (int z = -xMin; z <= xMin; z++) {
                    IBlockState state = creature.world.getBlockState(creature.getPosition().add(x, y, z));
                    boolean cantFitInWater = !spawner.getCanSpawnInWater() || state.getMaterial() != Material.WATER;
                    boolean isValidSpot = state.getMaterial() == Material.AIR
                            || state.getMaterial() == Material.PLANTS
                            || state.getMaterial() == Material.VINE;
                    if (!isValidSpot) return false;
                }
            }
        }
        return true;
    }

    private boolean testOtherCreatures(RiftCreature creature, int densityLimit) {
        List<RiftCreature> creatureList = creature.world.getEntitiesWithinAABB(creature.getClass(), creature.getEntityBoundingBox().grow(64D), new Predicate<RiftCreature>() {
            @Override
            public boolean apply(@Nullable RiftCreature input) {
                return !input.isTamed();
            }
        });
        return creatureList.size() <= densityLimit;
    }
}
