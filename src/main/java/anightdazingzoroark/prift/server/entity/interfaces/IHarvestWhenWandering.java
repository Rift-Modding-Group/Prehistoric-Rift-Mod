package anightdazingzoroark.prift.server.entity.interfaces;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public interface IHarvestWhenWandering {
    List<String> blocksToHarvest();
    default boolean isValidBlockToHarvest(World world, BlockPos pos) {
        boolean flag = false;
        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        for (String blockEntry : this.blocksToHarvest()) {
            int blockIdFirst = blockEntry.indexOf(":");
            int blockIdSecond = blockEntry.indexOf(":", blockIdFirst + 1);
            int blockData = Integer.parseInt(blockEntry.substring(blockIdSecond + 1));
            if (!flag) flag = Block.getBlockFromName(blockEntry.substring(0, blockIdSecond)).equals(block) && (blockData == -1 || block.getMetaFromState(blockState) == blockData);
        }
        return flag;
    }
    default int harvestRange() {
        return 2;
    }
    boolean isHarvesting();
    void setHarvesting(boolean value);
    void harvestBlock(BlockPos pos);
}
