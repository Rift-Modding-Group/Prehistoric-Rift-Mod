package anightdazingzoroark.prift.server.blocks;

import anightdazingzoroark.prift.client.ui.creatureBoxScreen.RiftCreatureBoxScreen;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.NewPlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftOpenCreatureBoxNoCreaturesMenu;
import anightdazingzoroark.prift.server.tileentities.RiftNewTileEntityCreatureBox;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import anightdazingzoroark.riftlib.ui.RiftLibUIHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;

public class RiftCreatureBox extends Block implements ITileEntityProvider {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final int maxDeployableCreatures = 10;

    public RiftCreatureBox() {
        super(Material.ROCK);
        this.setResistance(69420666F);
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
        return new RiftNewTileEntityCreatureBox();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (!world.isRemote && placer instanceof EntityPlayer) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (!(tileEntity instanceof RiftNewTileEntityCreatureBox)) return;
            RiftNewTileEntityCreatureBox teCreatureBox = (RiftNewTileEntityCreatureBox) tileEntity;
            teCreatureBox.setOwner((EntityPlayer) placer);
            teCreatureBox.setUniqueID(UUID.randomUUID());
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            RiftNewTileEntityCreatureBox tileEntity = (RiftNewTileEntityCreatureBox) worldIn.getTileEntity(pos);

            if (tileEntity == null) return false;

            RiftLibUIHelper.showUI(playerIn, new RiftCreatureBoxScreen(pos));

            /*
            if (playerIn.isSneaking()) {
                /*
                //highlight all creatures deployed
                for (RiftCreature creature : tileEntity.getCreatures()) {
                    if (creature == null) continue;
                    RiftCreature actualCreature = (RiftCreature) RiftUtil.getEntityFromUUID(worldIn, creature.getUniqueID());
                    if (actualCreature != null) {
                        actualCreature.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 100));
                    }
                }
            }
            else {
                if (NewPlayerTamedCreaturesHelper.getPlayerPartyNBT(playerIn).isEmpty()
                        && NewPlayerTamedCreaturesHelper.getCreatureBoxStorage(playerIn).isEmpty() && tileEntity.getCreatures().isEmpty()) {
                    RiftMessages.WRAPPER.sendTo(new RiftOpenCreatureBoxNoCreaturesMenu(playerIn), (EntityPlayerMP) playerIn);
                }
                else RiftLibUIHelper.showUI(playerIn, new RiftCreatureBoxScreen(pos));
            }
            */
        }
        return true;
    }
}
