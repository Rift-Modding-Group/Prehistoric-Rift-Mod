package anightdazingzoroark.prift.server.blocks;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.PopupFromCreatureBox;
import anightdazingzoroark.prift.server.message.RiftCreatureBoxSneak;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class RiftCreatureBox extends Block implements ITileEntityProvider {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

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
        return new RiftTileEntityCreatureBox();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            IPlayerTamedCreatures tamedCreatures = playerIn.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
            RiftTileEntityCreatureBox tileEntity = (RiftTileEntityCreatureBox) worldIn.getTileEntity(pos);
            if (playerIn.isSneaking()) RiftMessages.WRAPPER.sendToServer(new RiftCreatureBoxSneak(pos));
            else {
                if (tamedCreatures.getPartyCreatures(worldIn).isEmpty() && tamedCreatures.getBoxCreatures(worldIn).isEmpty() && tileEntity.getCreatures().isEmpty()) {
                    ClientProxy.popupFromRadial = PopupFromCreatureBox.NO_CREATURES;
                    playerIn.openGui(RiftInitialize.instance, ServerProxy.GUI_MENU_FROM_CREATURE_BOX, worldIn, 0, 0, 0);
                }
                else {
                    ClientProxy.creatureBoxBlockPos = pos;
                    playerIn.openGui(RiftInitialize.instance, ServerProxy.GUI_CREATURE_BOX, worldIn, 0, 0, 0);
                }
            }
        }
        return true;
    }
}
