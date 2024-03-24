package anightdazingzoroark.prift.compat.mysticalmechanics.blocks;

import anightdazingzoroark.prift.client.creativetab.RiftCreativeTabs;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityLeadPoweredCrank;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
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
    public BlockLeadPoweredCrank() {
        super(Material.ROCK);
        this.setCreativeTab(RiftCreativeTabs.creativeItemsTab);
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
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return world.isSideSolid(pos.down(), EnumFacing.UP);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemStack = playerIn.getHeldItem(hand);
        TileEntityLeadPoweredCrank crank = (TileEntityLeadPoweredCrank)worldIn.getTileEntity(pos);
        AxisAlignedBB detectArea = new AxisAlignedBB(pos.getX() - 7, pos.getY() - 7, pos.getZ() - 7, pos.getX() + 7, pos.getY() + 7, pos.getZ() + 7);
        RiftCreature creatureUser = null;

        if (crank != null) {
            //find creature within reach
            List<String> vUserList = Arrays.asList(GeneralConfig.lpcUsers);
            search: for (String mobIdentifier : vUserList) {
                for (RiftCreature creature : worldIn.getEntitiesWithinAABB(RiftCreature.class, detectArea)) {
                    if (EntityList.getKey(creature).toString().equals(mobIdentifier)) {
                        if (creature.getLeashed() && creature.getLeashHolder() == playerIn) {
                            creatureUser = creature;
                            break search;
                        }
                    }
                }
            }

            //attach creature
            if (((itemStack.getItem() instanceof ItemLead) && creatureUser != null) || creatureUser != null) {
                if (!crank.getHasLead()) {
                    creatureUser.clearLeashed(true, false);
                    crank.setWorker(creatureUser);
                    if (!worldIn.isRemote) playerIn.sendStatusMessage(new TextComponentTranslation("action.set_creature_workstation_success"), false);
                }
            }
        }

        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            TileEntityLeadPoweredCrank leadPoweredCrank = (TileEntityLeadPoweredCrank)worldIn.getTileEntity(pos);
            if (leadPoweredCrank.getWorker() != null) {
                leadPoweredCrank.getWorker().clearWorkstation(true);
            }
        }
        super.breakBlock(worldIn, pos, state);
    }
}
