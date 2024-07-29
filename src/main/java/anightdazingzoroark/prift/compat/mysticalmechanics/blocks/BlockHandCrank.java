package anightdazingzoroark.prift.compat.mysticalmechanics.blocks;

import anightdazingzoroark.prift.client.creativetab.RiftCreativeTabs;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityBlowPoweredTurbine;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityHandCrank;
import mysticalmechanics.block.BlockGearbox;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockHandCrank extends Block implements ITileEntityProvider {
    public static final PropertyDirection FACING = PropertyDirection.create("facing");

    public BlockHandCrank() {
        super(Material.WOOD);
        this.setCreativeTab(RiftCreativeTabs.creativeItemsTab);
        this.setHardness(2.0f);
        this.setResistance(5.0f);
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
    public IBlockState getStateFromMeta(int meta){
        return getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta));
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityHandCrank();
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

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntityHandCrank handCrank = (TileEntityHandCrank) worldIn.getTileEntity(pos);
        if (handCrank != null) {
            if (!worldIn.isRemote && handCrank.getAtMaxPowerTimer() <= 0) {
                if (!playerIn.isCreative()) playerIn.getFoodStats().setFoodLevel(playerIn.getFoodStats().getFoodLevel() - 1);
                handCrank.setPower(15f);
                handCrank.setMaxPowerTimer(100);
            }
        }
        return true;
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (!worldIn.isRemote) {
            //break turbine
            TileEntityHandCrank handCrank = (TileEntityHandCrank) worldIn.getTileEntity(pos);
            if (handCrank != null) handCrank.onBreakCrank();
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(BlockGearbox.facing, facing);
    }
}
