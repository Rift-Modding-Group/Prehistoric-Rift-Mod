package anightdazingzoroark.prift.compat.mysticalmechanics.blocks;

import anightdazingzoroark.prift.compat.mysticalmechanics.items.RiftMMItems;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualExtractorTop;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockSemiManualExtractorTop extends BlockSemiManualBaseTop {
    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntitySemiManualExtractorTop();
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(RiftMMItems.SEMI_MANUAL_EXTRACTOR);
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (!worldIn.isRemote) {
            //drop item
            if (!player.isCreative()) {
                EntityItem droppedItem = new EntityItem(worldIn);
                droppedItem.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
                droppedItem.setItem(new ItemStack(RiftMMItems.SEMI_MANUAL_EXTRACTOR));
                worldIn.spawnEntity(droppedItem);
            }
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }
}
