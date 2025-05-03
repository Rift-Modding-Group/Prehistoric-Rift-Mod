package anightdazingzoroark.prift.server.entity.interfaces;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
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
    boolean canHarvest();
    void setCanHarvest(boolean value);
    default void harvestBlock(RiftCreature creature, BlockPos pos) {
        int xMin = pos.getX() + (int)this.breakRange().minX;
        int xMax = pos.getX() + (int)this.breakRange().maxX;
        int yMin = pos.getY() + (int)this.breakRange().minY;
        int yMax = pos.getY() + (int)this.breakRange().maxY;
        int zMin = pos.getZ() + (int)this.breakRange().minZ;
        int zMax = pos.getZ() + (int)this.breakRange().maxZ;
        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    BlockPos testPos = new BlockPos(x, y, z);
                    if (this.isValidBlockToHarvest(creature.world, testPos)) {
                        IBlockState blockState = creature.world.getBlockState(testPos);
                        Block block = blockState.getBlock();

                        //get drops
                        List<ItemStack> drops = block.getDrops(creature.world, testPos, blockState, 0);
                        for (ItemStack stack : drops) creature.creatureInventory.addItem(stack);

                        creature.world.destroyBlock(testPos, false);
                    }
                }
            }
        }
    }
    default AxisAlignedBB breakRange() {
        return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    }
    default void writeHarvestWanderDataToNBT(NBTTagCompound compound) {
        compound.setBoolean("CanHarvest", this.canHarvest());
    }
    default void readHarvestWanderDataFromNBT(NBTTagCompound compound) {
        this.setCanHarvest(compound.getBoolean("CanHarvest"));
    }
}
