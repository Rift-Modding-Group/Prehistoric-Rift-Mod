package anightdazingzoroark.prift.server.world;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.blocks.RiftBerryBush;
import anightdazingzoroark.prift.server.blocks.RiftBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Arrays;
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

            if (RiftUtil.posInBiomeListString(Arrays.asList(biomeTags), world, pos)
                && world.isAirBlock(pos)
                && state.getBlock().canPlaceBlockAt(world, pos)) world.setBlockState(pos, state, 2);
        }
    }
}
