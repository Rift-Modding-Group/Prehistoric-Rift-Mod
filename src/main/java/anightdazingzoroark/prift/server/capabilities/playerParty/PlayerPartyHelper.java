package anightdazingzoroark.prift.server.capabilities.playerParty;

import anightdazingzoroark.prift.server.capabilities.CapabilitySyncDirection;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftSyncPlayerParty;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerPartyHelper {
    public static final int maxSize = 6;

    public static IPlayerParty getPlayerParty(EntityPlayer player) {
        if (player == null) return null;
        return player.getCapability(PlayerPartyProvider.PLAYER_PARTY_CAPABILITY, null);
    }

    public static void syncPlayerParty(EntityPlayer player, CapabilitySyncDirection direction) {
        if (player == null || !player.world.isRemote) return;
        if (direction == CapabilitySyncDirection.SERVER_TO_CLIENT) {
            RiftMessages.WRAPPER.sendToServer(new RiftSyncPlayerParty(player, CapabilitySyncDirection.SERVER_TO_CLIENT));
        }
        else if (direction == CapabilitySyncDirection.CLIENT_TO_SERVER) {
            IPlayerParty playerParty = getPlayerParty(player);
            RiftMessages.WRAPPER.sendToServer(new RiftSyncPlayerParty(player, CapabilitySyncDirection.CLIENT_TO_SERVER, playerParty.getPartyNBTForSync()));

            //reset tp info afterwards
            playerParty.resetTeleportationMarker();
        }
    }

    public static void addCreatureToParty(EntityPlayer player, RiftCreature creature) {
        if (player == null || player.world.isRemote) return;
        getPlayerParty(player).addPartyMember(creature);
    }

    public static void updatePartyCreature(EntityPlayer player, RiftCreature creature) {
        if (player == null || player.world.isRemote) return;
        getPlayerParty(player).updatePartyMember(creature);
    }

    public static boolean canAddToParty(EntityPlayer player) {
        if (player == null) return false;
        return getPlayerParty(player).canAddToParty();
    }
}
