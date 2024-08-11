package anightdazingzoroark.prift.server.creatureSpawning;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.Coelacanth;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber
public class RiftCreatureSpawning {
    private final List<BlockPos> spawnPositions = new ArrayList<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.isRemote) return;

        WorldServer world = (WorldServer) event.world;

        if (!world.getGameRules().getBoolean("doMobSpawning")) return;

        if (event.phase == TickEvent.Phase.END && world.getTotalWorldTime() % 400L == 0) {
            this.spawnPositions.clear();
            ChunkProviderServer chunkProvider = world.getChunkProvider();

            //add positions
            for (Map.Entry<Long, Chunk> entry : chunkProvider.loadedChunks.entrySet()) {
                Chunk chunk = entry.getValue();
                this.spawnPositions.add(getRandomChunkPosition(world, chunk.x, chunk.z));
            }

            //remove positions too close to players
            List<BlockPos> playerPositions = new ArrayList<>();
            for (EntityPlayer player : world.playerEntities) {
                playerPositions.add(player.getPosition());
            }

            Iterator<BlockPos> iterator = this.spawnPositions.iterator();
            while (iterator.hasNext()) {
                BlockPos pos = iterator.next();
                boolean tooClose = playerPositions.stream().anyMatch(refPos -> pos.distanceSq(refPos) <= 256);
                if (tooClose) {
                    iterator.remove();
                }
            }

            //first get list of biomes of all loaded chunks
            List<Biome> biomeList = new ArrayList<>();
            for (BlockPos pos : this.spawnPositions) {
                Biome biome = world.getBiome(pos);
                if (!biomeList.contains(biome)) biomeList.add(biome);
            }
            RiftCreatureSpawnLists.createSpawnerList(biomeList, world); //generate

            //finally spawn the creatures based on spawnPositions and biomeSpawnerList
            this.spawnFromBiomeSpawnerLists(world, RiftCreatureSpawnLists.landBiomeSpawnerList);
            this.spawnFromBiomeSpawnerLists(world, RiftCreatureSpawnLists.waterBiomeSpawnerList);
            this.spawnFromBiomeSpawnerLists(world, RiftCreatureSpawnLists.airBiomeSpawnerList);
            this.spawnFromBiomeSpawnerLists(world, RiftCreatureSpawnLists.caveBiomeSpawnerList);
        }
    }

    private void spawnFromBiomeSpawnerLists(World world, List<RiftCreatureSpawnLists.BiomeSpawner> biomeSpawners) {
        for (BlockPos pos : this.spawnPositions) {
            for (RiftCreatureSpawnLists.BiomeSpawner biomeSpawner : biomeSpawners) {
                if (world.getBiome(pos).equals(biomeSpawner.biome)) {

                    RiftCreatureSpawnLists.Spawner spawnerToSpawn = biomeSpawner.getSpawnerRandomly();
                    int spawnAmnt = spawnerToSpawn.getSpawnAmnt();
                    for (int x = 0; x < spawnAmnt; x++) {
                        RiftCreature creatureToSpawn = spawnerToSpawn.getCreatureType().invokeClass(world);
                        creatureToSpawn.setPosition(pos.getX(), pos.getY(), pos.getZ());
                        creatureToSpawn.onInitialSpawn(world.getDifficultyForLocation(pos), null);

                        boolean spawnSeeSky = spawnerToSpawn.getMustSeeSky() == this.canSeeSkySpawn(creatureToSpawn.world, creatureToSpawn.getPosition());
                        boolean spawnInRain = !spawnerToSpawn.getMustSpawnInRain() || world.isRaining();

                        if (this.canFitInArea(creatureToSpawn)
                                && this.testOtherCreatures(creatureToSpawn, spawnerToSpawn.getDensityLimit())
                                && this.canSpawnAtPos(creatureToSpawn, spawnerToSpawn)
                                && spawnerToSpawn.getYLevelRange().get(0) <= creatureToSpawn.getPosition().getY()
                                && spawnerToSpawn.getYLevelRange().get(1) >= creatureToSpawn.getPosition().getY()
                                && world.provider.getDimension() == spawnerToSpawn.getDimensionId()
                                && spawnSeeSky
                                && spawnInRain) {
                            System.out.println(creatureToSpawn);
                            world.spawnEntity(creatureToSpawn);
                        }
                    }
                }
            }
        }
    }

    private static BlockPos getRandomChunkPosition(World worldIn, int x, int z) {
        Chunk chunk = worldIn.getChunk(x, z);
        int i = x * 16 + worldIn.rand.nextInt(16);
        int j = z * 16 + worldIn.rand.nextInt(16);
        int k = MathHelper.roundUp(chunk.getHeight(new BlockPos(i, 0, j)) + 1, 16);
        int l = worldIn.rand.nextInt(k > 0 ? k : chunk.getTopFilledSegment() + 16 - 1);
        return new BlockPos(i, l, j);
    }

    //other spawn conditions
    private boolean canSpawnAtLightLevel(World world, BlockPos pos, int min, int max) {
        int lightAtPos = world.getLight(pos);
        return lightAtPos >= min && lightAtPos <= max;
    }

    private boolean canFitInArea(RiftCreature creature) {
        int xMin = -(int)Math.floor(creature.width / 2);
        for (int x = xMin; x <= -xMin; x++) {
            for (int y = 0; y < (int)Math.ceil(creature.height); y++) {
                for (int z = -xMin; z <= -xMin; z++) {
                    IBlockState state = creature.world.getBlockState(creature.getPosition().add(x, y, z));
                    if (state.getMaterial() != Material.AIR) return false;
                }
            }
        }
        return true;
    }

    private boolean canSpawnAtPos(RiftCreature creature, RiftCreatureSpawnLists.Spawner spawner) {
        boolean spawnAboveBlock = spawner.getCanSpawnOnLand() && this.canSpawnAboveBlock(creature.world, creature.getPosition(), spawner.getSpawnBlocksWhitelist());
        boolean spawnInWater = spawner.getCanSpawnInWater() && creature.world.getBlockState(creature.getPosition()).getMaterial() == Material.WATER;
        boolean spawnInAir = spawner.getCanSpawnInAir() && creature.world.getBlockState(creature.getPosition().down()).getMaterial() == Material.AIR;
        if (creature instanceof Coelacanth) {
            System.out.println("coelacanth material: " + creature.world.getBlockState(creature.getPosition()).getMaterial());
            System.out.println("coelacanth pos: " + creature.getPosition());
        }
        return spawnAboveBlock || spawnInWater || spawnInAir;
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

    private boolean canSeeSkySpawn(World world, BlockPos pos) {
        boolean flag = true;
        for (int y = pos.getY(); y <= 256; y++) {
            if (flag) {
                BlockPos newPos = new BlockPos(pos.getX(), y, pos.getZ());
                flag = world.getBlockState(newPos).getMaterial() == Material.AIR || world.getBlockState(newPos).getMaterial() == Material.LEAVES || world.getBlockState(newPos).getMaterial() == Material.GLASS;
            }
            else break;
        }
        return flag;
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
