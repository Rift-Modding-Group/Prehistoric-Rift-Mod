package anightdazingzoroark.prift.server.blocks;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.enums.PopupFromCreatureBox;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RiftCreatureBox extends Block {
    public RiftCreatureBox() {
        super(Material.ROCK);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            IPlayerTamedCreatures tamedCreatures = playerIn.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
            if (tamedCreatures.getPartyCreatures(worldIn).isEmpty() && tamedCreatures.getBoxCreatures(worldIn).isEmpty()) {
                playerIn.openGui(RiftInitialize.instance, ServerProxy.GUI_MENU_FROM_CREATURE_BOX, worldIn, PopupFromCreatureBox.NO_CREATURES.ordinal(), 0, 0);
            }
            else playerIn.openGui(RiftInitialize.instance, ServerProxy.GUI_CREATURE_BOX, worldIn, 0, 0, 0);
        }
        return true;
    }
}
