package anightdazingzoroark.prift.server.world;

import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.blocks.RiftBerryBush;
import anightdazingzoroark.prift.server.blocks.RiftBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RiftPlantGenerator implements IWorldGenerator {
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() == 0) {
            this.generatePlant(RiftBlocks.PYROBERRY_BUSH, world, random, GeneralConfig.pyroberryWeight,chunkX * 16, chunkZ * 16, GeneralConfig.pyroberryBiomes);
            this.generatePlant(RiftBlocks.CRYOBERRY_BUSH, world, random, GeneralConfig.cryoberryWeight,chunkX * 16, chunkZ * 16, GeneralConfig.cryoberryBiomes);
        }
    }

    private void generatePlant(Block block, World world, Random random, int weight, int x, int z, String[] biomeTags) {
        for (int i = 0; i < weight; i++) {
            int posX = x + random.nextInt(16);
            int posY = random.nextInt(256);
            int posZ = z + random.nextInt(16);
            BlockPos pos = new BlockPos(posX, posY, posZ);
            IBlockState state;

            if (block instanceof RiftBerryBush) {
                RiftBerryBush berryBush = (RiftBerryBush)block;
                state = berryBush.withAge(3);
            }
            else state = block.getDefaultState();

            List<Biome> biomeList = new ArrayList<>();
            for (String biomeEntry : biomeTags) {
                if (biomeEntry.charAt(0) != '-') {
                    int partOne = biomeEntry.indexOf(":");
                    String spawnerType = biomeEntry.substring(0, partOne);
                    String entry = biomeEntry.substring(partOne + 1);
                    if (spawnerType.equals("biome")) {
                        for (Biome biome : Biome.REGISTRY) {
                            if (biome.getRegistryName().toString().equals(entry) && world.getBiome(pos).equals(biome) && world.isAirBlock(pos) && state.getBlock().canPlaceBlockAt(world, pos)) {
                                biomeList.add(biome);
                            }
                        }
                    }
                    else if (spawnerType.equals("tag")) {
                        for (Biome biome : Biome.REGISTRY) {
                            if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.getType(entry)) && world.getBiome(pos).equals(biome) && world.isAirBlock(pos) && state.getBlock().canPlaceBlockAt(world, pos)) {
                                biomeList.add(biome);
                            }
                        }
                    }
                }
                else {
                    int partOne = biomeEntry.indexOf(":");
                    String spawnerType = biomeEntry.substring(1, partOne);
                    String entry = biomeEntry.substring(partOne + 1);
                    if (spawnerType.equals("biome")) {
                        for (Biome biome : Biome.REGISTRY) {
                            if (biome.getRegistryName().equals(entry) && biomeList.contains(biome)) {
                                biomeList.remove(biome);
                            }
                        }
                    }
                    else if (spawnerType.equals("tag")) {
                        for (Biome biome : Biome.REGISTRY) {
                            if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.getType(entry)) && biomeList.contains(biome)) {
                                biomeList.remove(biome);
                            }
                        }
                    }
                }
            }
            if (!biomeList.isEmpty()) world.setBlockState(pos, state, 2);
        }
    }
}
