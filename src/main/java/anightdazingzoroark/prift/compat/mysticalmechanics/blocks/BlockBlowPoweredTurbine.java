package anightdazingzoroark.prift.compat.mysticalmechanics.blocks;

import anightdazingzoroark.prift.client.creativetab.RiftCreativeTabs;
import anightdazingzoroark.prift.compat.mysticalmechanics.items.RiftMMItems;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityBlowPoweredTurbine;
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
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockBlowPoweredTurbine extends Block implements ITileEntityProvider  {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockBlowPoweredTurbine() {
        super(Material.IRON);
        this.setCreativeTab(RiftCreativeTabs.creativeItemsTab);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.setHardness(5.0f);
        this.setResistance(10.0f);
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("pickaxe", 2);
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
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityBlowPoweredTurbine();
    }

    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    public boolean isFullCube(IBlockState state) {
        return false;
    }

    //this is for testing purposes
//    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
//        TileEntityBlowPoweredTurbine turbine = (TileEntityBlowPoweredTurbine) worldIn.getTileEntity(pos);
//        if (turbine != null) {
//            if (!worldIn.isRemote) turbine.setPower(20f);
//        }
//        return true;
//    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (!worldIn.isRemote) {
            //break turbine
            TileEntityBlowPoweredTurbine turbine = (TileEntityBlowPoweredTurbine) worldIn.getTileEntity(pos);
            if (turbine != null) turbine.onBreakTurbine();

            //break all parts
            for (int width = -1; width <= 1; width++) {
                for (int height = -1; height <= 1; height++) {
                    int widthX = state.getValue(BlockHorizontal.FACING).getAxis().equals(EnumFacing.Axis.Z) ? width : state.getValue(BlockHorizontal.FACING).getAxisDirection().equals(EnumFacing.AxisDirection.POSITIVE) ? -1 : 1;
                    int widthZ = state.getValue(BlockHorizontal.FACING).getAxis().equals(EnumFacing.Axis.X) ? width : state.getValue(BlockHorizontal.FACING).getAxisDirection().equals(EnumFacing.AxisDirection.POSITIVE) ? -1 : 1;
                    BlockPos destroyPos = pos.offset(state.getValue(BlockHorizontal.FACING)).add(widthX, height, widthZ);
                    if (!destroyPos.equals(pos)) worldIn.destroyBlock(destroyPos, false);
                }
            }

            //drop item
            if (!player.isCreative()) {
                EntityItem droppedItem = new EntityItem(worldIn);
                droppedItem.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
                droppedItem.setItem(new ItemStack(RiftMMItems.BLOW_POWERED_TURBINE));
                worldIn.spawnEntity(droppedItem);
            }
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(RiftMMItems.BLOW_POWERED_TURBINE);
    }
}
