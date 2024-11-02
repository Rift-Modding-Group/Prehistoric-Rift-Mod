package anightdazingzoroark.prift.server.world;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.structures.RiftedStoneMound;
import anightdazingzoroark.prift.server.structures.RiftedStoneVein;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Arrays;
import java.util.Random;

public class RiftStructureGenerator implements IWorldGenerator {
    private static final RiftedStoneMound RIFTED_STONE_MOUND = new RiftedStoneMound();
    private static final RiftedStoneVein RIFTED_STONE_VEIN = new RiftedStoneVein();

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() == 0) {
            //for rifted stone mounds
            for (int i = 0; i < GeneralConfig.riftedMoundWeight; i++) {
                int posX = (chunkX * 16) + random.nextInt(16);
                int posY = random.nextInt(256);
                int posZ = (chunkZ * 16) + random.nextInt(16);
                BlockPos pos = new BlockPos(posX, posY, posZ);

                boolean isWaterBelow = world.getBlockState(pos.down()).getMaterial() == Material.WATER;
                boolean suitableForGenerating = world.getBlockState(pos).getMaterial() == Material.AIR
                        && world.getBlockState(pos.down()).getMaterial() != Material.AIR
                        && world.getBlockState(pos.down()).getMaterial() != Material.LEAVES
                        && world.getBlockState(pos.down()).getMaterial() != Material.PLANTS;

                if (RiftUtil.posInBiomeListString(Arrays.asList(GeneralConfig.riftedMoundBiomes), world, pos)
                    && !isWaterBelow
                    && suitableForGenerating) RIFTED_STONE_MOUND.generate(world, random, pos);
            }

            //for rifted stone veins
            for (int i = 0; i < GeneralConfig.riftedVeinWeight; i++) {
                int posX = (chunkX * 16) + random.nextInt(16);
                int posY = RiftUtil.randomInRange(Integer.parseInt(GeneralConfig.riftedVeinGenHeight[0]), Integer.parseInt(GeneralConfig.riftedVeinGenHeight[1]));
                int posZ = (chunkZ * 16) + random.nextInt(16);
                BlockPos pos = new BlockPos(posX, posY, posZ);
                if (world.getBlockState(pos).getMaterial() != Material.AIR) RIFTED_STONE_VEIN.generate(world, random, pos);
            }
        }
    }
}
