package anightdazingzoroark.prift.server.blocks;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.creativetab.RiftCreativeTabs;
import anightdazingzoroark.prift.server.RiftGui;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityFeedingTrough;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class RiftFeedingTroughBlock extends Block implements ITileEntityProvider {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    protected RiftFeedingTroughBlock() {
        super(Material.WOOD);
        this.setCreativeTab(RiftCreativeTabs.creativeItemsTab);
        this.setHardness(2.0f);
        this.setResistance(5.0f);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
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
        return new RiftTileEntityFeedingTrough();
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        double xMin = state.getValue(FACING).equals(EnumFacing.NORTH) || state.getValue(FACING).equals(EnumFacing.SOUTH) ? 0 : 0.1875;
        double xBreak = state.getValue(FACING).equals(EnumFacing.NORTH) || state.getValue(FACING).equals(EnumFacing.SOUTH) ? 1 : 0.8125;
        double zMin = state.getValue(FACING).equals(EnumFacing.EAST) || state.getValue(FACING).equals(EnumFacing.WEST) ? 0 : 0.1875;
        double zBreak = state.getValue(FACING).equals(EnumFacing.EAST) || state.getValue(FACING).equals(EnumFacing.WEST) ? 1 : 0.8125;
        return new AxisAlignedBB(xMin, 0, zMin, xBreak, 0.5625, zBreak);
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
            playerIn.openGui(RiftInitialize.instance, RiftGui.GUI_FEEDING_TROUGH, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }
}
