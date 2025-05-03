package anightdazingzoroark.prift.compat.mysticalmechanics.blocks;

import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public abstract class BlockSemiManualBase extends Block implements ITileEntityProvider {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockSemiManualBase() {
        super(Material.IRON);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.setHardness(5.0F);
        this.setResistance(10.0F);
        this.setSoundType(SoundType.METAL);
    }

    @Override
    public BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state){
        return state.getValue(FACING).getIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.byIndex(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y) enumfacing = EnumFacing.NORTH;
        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    public abstract TileEntity createNewTileEntity(World worldIn, int meta);

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    public boolean isFullCube(IBlockState state) {
        return false;
    }

    public abstract ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player);

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (!worldIn.isRemote) {
            //destroy upper block
            Block topBlock = worldIn.getBlockState(pos.up()).getBlock();

            if (topBlock instanceof BlockSemiManualBaseTop) {
                worldIn.destroyBlock(pos.up(), false);
            }

            //drop items from inventory
            this.dropInventory(worldIn, pos);
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    public void dropInventory(World worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntitySemiManualBase) {
            TileEntitySemiManualBase semiManualBase = (TileEntitySemiManualBase)tileEntity;
            IItemHandler itemHandler = semiManualBase.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            for (int x = 0; x < itemHandler.getSlots(); x++) {
                ItemStack stack = itemHandler.getStackInSlot(x);
                if (!stack.isEmpty()) {
                    EntityItem droppedItem = new EntityItem(worldIn);
                    droppedItem.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
                    droppedItem.setItem(stack);
                    worldIn.spawnEntity(droppedItem);
                }
            }
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
    }
}
