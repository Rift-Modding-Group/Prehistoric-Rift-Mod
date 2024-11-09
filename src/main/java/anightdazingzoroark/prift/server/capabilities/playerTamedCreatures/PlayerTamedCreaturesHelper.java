package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftUpgradePlayerBox;
import anightdazingzoroark.prift.server.message.RiftUpgradePlayerParty;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class PlayerTamedCreaturesHelper {
    //player party and creature box
    private static IPlayerTamedCreatures getPlayerTamedCreatures(EntityPlayer player) {
        if (player == null) return null;
        return player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
    }

    public static List<RiftCreature> getPlayerParty(EntityPlayer player) {
        return getPlayerTamedCreatures(player).getPartyCreatures(player.world);
    }

    public static int getPartySizeLevel(EntityPlayer player) {
        return getPlayerTamedCreatures(player).getPartySizeLevel();
    }

    public static void upgradePlayerParty(EntityPlayer player, int value) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).setPartySizeLevel(value);
            RiftMessages.WRAPPER.sendToServer(new RiftUpgradePlayerParty(player, value));
        }
        else {
            getPlayerTamedCreatures(player).setPartySizeLevel(value);
            RiftMessages.WRAPPER.sendToAll(new RiftUpgradePlayerParty(player, value));
        }
    }

    public static int getBoxSizeLevel(EntityPlayer player) {
        return getPlayerTamedCreatures(player).getBoxSizeLevel();
    }

    public static void upgradePlayerBox(EntityPlayer player, int value) {
        if (player.world.isRemote) {
            getPlayerTamedCreatures(player).setBoxSizeLevel(value);
            RiftMessages.WRAPPER.sendToServer(new RiftUpgradePlayerBox(player, value));
        }
        else {
            getPlayerTamedCreatures(player).setBoxSizeLevel(value);
            RiftMessages.WRAPPER.sendToAll(new RiftUpgradePlayerBox(player, value));
        }
    }

    public static void upgradeCreatureBoxWandererCount(World world, BlockPos pos, int value) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof RiftTileEntityCreatureBox) {
            RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) tileEntity;
            creatureBox.setCreatureAmntLevel(value);
        }
    }
}
