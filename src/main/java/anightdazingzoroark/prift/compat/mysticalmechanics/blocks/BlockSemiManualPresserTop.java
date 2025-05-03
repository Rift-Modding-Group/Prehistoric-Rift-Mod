package anightdazingzoroark.prift.compat.mysticalmechanics.blocks;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.items.RiftMMItems;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualPresserTop;
import anightdazingzoroark.prift.server.RiftGui;
import anightdazingzoroark.prift.server.ServerProxy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockSemiManualPresserTop extends BlockSemiManualBaseTop {
    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntitySemiManualPresserTop();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            playerIn.openGui(RiftInitialize.instance, RiftGui.GUI_SEMI_MANUAL_PRESSER, worldIn, pos.getX(), pos.getY() - 1, pos.getZ());
        }
        return true;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(RiftMMItems.SEMI_MANUAL_PRESSER);
    }
}
