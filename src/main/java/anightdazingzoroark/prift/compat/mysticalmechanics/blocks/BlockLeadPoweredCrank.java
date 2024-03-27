package anightdazingzoroark.prift.compat.mysticalmechanics.blocks;

import anightdazingzoroark.prift.client.creativetab.RiftCreativeTabs;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityLeadPoweredCrank;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.ILeadWorkstationUser;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import mysticalmechanics.block.BlockGearbox;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemLead;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class BlockLeadPoweredCrank extends Block implements ITileEntityProvider {
    public static final PropertyDirection FACING = PropertyDirection.create("facing");

    public BlockLeadPoweredCrank() {
        super(Material.ROCK);
        this.setCreativeTab(RiftCreativeTabs.creativeItemsTab);
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
        return getDefaultState().withProperty(FACING, EnumFacing.UP);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityLeadPoweredCrank();
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
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return new AxisAlignedBB(0.375, 0, 0.375, 0.625, 1, 0.625);
    }

    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.UP);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemStack = playerIn.getHeldItem(hand);
        TileEntityLeadPoweredCrank crank = (TileEntityLeadPoweredCrank)worldIn.getTileEntity(pos);
        AxisAlignedBB detectArea = new AxisAlignedBB(pos.getX() - 7, pos.getY() - 7, pos.getZ() - 7, pos.getX() + 7, pos.getY() + 7, pos.getZ() + 7);
        RiftCreature creatureUser = null;

        if (crank != null) {
            //find creature within reach
            for (RiftCreature creature : worldIn.getEntitiesWithinAABB(RiftCreature.class, detectArea)) {
                if (creature instanceof ILeadWorkstationUser) {
                    ILeadWorkstationUser user = (ILeadWorkstationUser) creature;
                    if (creature.getLeashed() && creature.getLeashHolder() == playerIn && user.isAttachableForWork(pos)) {
                        creatureUser = creature;
                    }
                }
            }

            //attach creature
            if (((itemStack.getItem() instanceof ItemLead) && creatureUser != null) || creatureUser != null) {
                if (!crank.getHasLead()) {
                    creatureUser.clearLeashed(true, false);
                    crank.setWorker(creatureUser);
                    crank.updateNeighbors();
                    if (!worldIn.isRemote) playerIn.sendStatusMessage(new TextComponentTranslation("action.set_creature_workstation_success"), false);
                }
            }
            else if (!(itemStack.getItem() instanceof ItemLead)) {
                if (crank.getHasLead()) {
                    crank.removeWorker();
                    if (!worldIn.isRemote) {
                        EntityItem droppedLead = new EntityItem(worldIn);
                        droppedLead.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
                        droppedLead.setItem(new ItemStack(Items.LEAD));
                        worldIn.spawnEntity(droppedLead);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (!worldIn.isRemote) {
            TileEntityLeadPoweredCrank leadPoweredCrank = (TileEntityLeadPoweredCrank)worldIn.getTileEntity(pos);
            if (leadPoweredCrank.getWorker() != null) {
                leadPoweredCrank.getWorker().clearWorkstation(true);
                leadPoweredCrank.onBreakCrank();

                EntityItem droppedLead = new EntityItem(worldIn);
                droppedLead.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
                droppedLead.setItem(new ItemStack(Items.LEAD));
                worldIn.spawnEntity(droppedLead);
            }
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }
}
