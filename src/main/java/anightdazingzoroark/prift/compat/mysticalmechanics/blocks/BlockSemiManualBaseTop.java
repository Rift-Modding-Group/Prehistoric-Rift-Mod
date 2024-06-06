package anightdazingzoroark.prift.compat.mysticalmechanics.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class BlockSemiManualBaseTop extends BlockSemiManualBase {
    @Nullable
    @Override
    public abstract TileEntity createNewTileEntity(World worldIn, int meta);

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (!worldIn.isRemote) {
            Block bottomBlock = worldIn.getBlockState(pos.down()).getBlock();

            if (bottomBlock instanceof BlockSemiManualBase) {
                worldIn.destroyBlock(pos.down(), false);
            }
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return null;
    }
}