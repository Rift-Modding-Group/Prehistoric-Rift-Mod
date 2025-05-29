package anightdazingzoroark.prift.server.structures;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.blocks.RiftBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public class RiftedStoneMound extends WorldGenerator {
    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        int maxRadius = RiftUtil.randomInRange(Integer.parseInt(GeneralConfig.riftedMoundSize[0]), Integer.parseInt(GeneralConfig.riftedMoundSize[1]));
        int height = RiftUtil.randomInRange(Integer.parseInt(GeneralConfig.riftedMoundSize[2]), Integer.parseInt(GeneralConfig.riftedMoundSize[3]));
        double angle = Math.toRadians(RiftUtil.randomInRange(-3, 3) * 5D);
        double leanFactor = RiftUtil.randomInRange(0.2D, 0.5D);

        for (int y = -3; y < height; y++) {
            int currentRadius = Math.max(0, maxRadius - Math.max(0, y) / 2);

            int xOffset = (int) (leanFactor * y * Math.cos(angle));
            int zOffset = (int) (leanFactor * y * Math.sin(angle));

            for (int x = -currentRadius; x <= currentRadius; x++) {
                for (int z = -currentRadius; z <= currentRadius; z++) {
                    if (x * x + z * z <= currentRadius * currentRadius) {
                        BlockPos blockPos = position.add(x + xOffset, y, z + zOffset);
                        IBlockState stalagmiteBlock = this.getBlockForBuilding(rand);

                        worldIn.setBlockState(blockPos, stalagmiteBlock, 2);
                    }
                }
            }
        }
        return true;
    }

    private IBlockState getBlockForBuilding(Random random) {
        int chance = random.nextInt(100);
        if (chance >= 95) return RiftBlocks.RIFT_SHARD_ORE.getDefaultState();
        else if (chance >= 50) return RiftBlocks.RIFTED_COBBLESTONE.getDefaultState();
        else return RiftBlocks.RIFTED_STONE.getDefaultState();
    }
}
