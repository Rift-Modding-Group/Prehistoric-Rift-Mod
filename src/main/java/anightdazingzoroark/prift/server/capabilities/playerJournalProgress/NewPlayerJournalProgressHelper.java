package anightdazingzoroark.prift.server.capabilities.playerJournalProgress;

import anightdazingzoroark.prift.server.capabilities.CapabilitySyncDirection;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftSyncPlayerJournalProgress;
import net.minecraft.entity.player.EntityPlayer;

public class NewPlayerJournalProgressHelper {
    public static IPlayerJournalProgress getPlayerJournalProgress(EntityPlayer player) {
        if (player == null) return null;
        return player.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
    }

    public static void syncJournalProgress(EntityPlayer player, CapabilitySyncDirection direction) {
        if (player == null || !player.world.isRemote) return;
        if (direction == CapabilitySyncDirection.SERVER_TO_CLIENT) {
            RiftMessages.WRAPPER.sendToServer(new RiftSyncPlayerJournalProgress(player, CapabilitySyncDirection.SERVER_TO_CLIENT));
        }
        else if (direction == CapabilitySyncDirection.CLIENT_TO_SERVER) {
            IPlayerJournalProgress journalProgress = getPlayerJournalProgress(player);
            RiftMessages.WRAPPER.sendToServer(new RiftSyncPlayerJournalProgress(player, CapabilitySyncDirection.CLIENT_TO_SERVER, journalProgress.getProgressAsNBTList()));
        }
    }
}
