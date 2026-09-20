package anightdazingzoroark.prift.server.world;

import anightdazingzoroark.prift.server.block.RiftBlocks;
import anightdazingzoroark.prift.server.block.SmokenutBush;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class RiftWorldGenerator implements IWorldGenerator {
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0 || random.nextInt(8) != 0) return;

        int originX = chunkX * 16 + 2 + random.nextInt(12);
        int originZ = chunkZ * 16 + 2 + random.nextInt(12);
        for (int attempt = 0; attempt < 8; attempt++) {
            int x = originX + random.nextInt(5) - 2;
            int z = originZ + random.nextInt(5) - 2;
            BlockPos plantPos = world.getHeight(new BlockPos(x, 0, z));
            IBlockState ground = world.getBlockState(plantPos.down());

            if (world.isAirBlock(plantPos) && (ground.getBlock() == Blocks.GRASS || ground.getBlock() == Blocks.DIRT)) {
                world.setBlockState(plantPos, RiftBlocks.SMOKENUT_BUSH.withAge(RiftBlocks.SMOKENUT_BUSH.getMaxAge())
                        .withProperty(SmokenutBush.HAS_SMOKENUTS, true), 2);
            }
        }
    }
}
