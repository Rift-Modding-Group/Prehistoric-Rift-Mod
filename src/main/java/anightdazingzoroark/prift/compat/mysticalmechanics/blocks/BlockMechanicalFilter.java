package anightdazingzoroark.prift.compat.mysticalmechanics.blocks;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityMechanicalFilter;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityMillstone;
import anightdazingzoroark.prift.server.RiftGui;
import anightdazingzoroark.prift.server.ServerProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class BlockMechanicalFilter extends Block implements ITileEntityProvider {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockMechanicalFilter() {
        super(Material.WOOD);
        this.setHardness(2.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.WOOD);
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
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing enumfacing = placer.getHorizontalFacing().getOpposite();
        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityMechanicalFilter();
    }

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

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            playerIn.openGui(RiftInitialize.instance, RiftGui.GUI_MECHANICAL_FILTER, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (!worldIn.isRemote) {
            this.dropInventory(worldIn, pos);
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    public void dropInventory(World worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntityMechanicalFilter) {
            TileEntityMechanicalFilter mechanicalFilter = (TileEntityMechanicalFilter)tileEntity;
            IItemHandler itemHandler = mechanicalFilter.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
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
}
