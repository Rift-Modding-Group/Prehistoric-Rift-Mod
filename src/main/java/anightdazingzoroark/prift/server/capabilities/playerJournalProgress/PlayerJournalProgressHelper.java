package anightdazingzoroark.prift.server.capabilities.playerJournalProgress;

import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.enums.CreatureCategory;
import anightdazingzoroark.prift.server.message.RiftJournalEditAll;
import anightdazingzoroark.prift.server.message.RiftJournalEditOne;
import anightdazingzoroark.prift.server.message.RiftMessages;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;
import java.util.Map;

public class PlayerJournalProgressHelper {
    public static IPlayerJournalProgress getPlayerJournalProgress(EntityPlayer player) {
        if (player == null) return null;
        return player.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
    }

    public static Map<RiftCreatureType, Boolean> getUnlockedCreatures(EntityPlayer player) {
        return getPlayerJournalProgress(player).getEncounteredCreatures();
    }

    public static List<CreatureCategory> getUnlockedCategories(EntityPlayer player) {
        return getPlayerJournalProgress(player).getUnlockedCategories();
    }

    public static void discoverCreature(EntityPlayer player, RiftCreatureType creatureType) {
        if (player.world.isRemote) {
            getPlayerJournalProgress(player).discoverCreature(creatureType);
            RiftMessages.WRAPPER.sendToServer(new RiftJournalEditOne(creatureType, true, false));
        }
        else {
            getPlayerJournalProgress(player).discoverCreature(creatureType);
            RiftMessages.WRAPPER.sendToAll(new RiftJournalEditOne(creatureType, true, false));
        }
    }

    public static void unlockCreature(EntityPlayer player, RiftCreatureType creatureType) {
        if (player.world.isRemote) {
            getPlayerJournalProgress(player).unlockCreature(creatureType);
            RiftMessages.WRAPPER.sendToServer(new RiftJournalEditOne(creatureType, true));
        }
        else {
            getPlayerJournalProgress(player).unlockCreature(creatureType);
            RiftMessages.WRAPPER.sendToAll(new RiftJournalEditOne(creatureType, true));
        }
    }

    public static void clearCreature(EntityPlayer player, RiftCreatureType creatureType) {
        if (player.world.isRemote) {
            getPlayerJournalProgress(player).clearCreature(creatureType);
            RiftMessages.WRAPPER.sendToServer(new RiftJournalEditOne(creatureType, false));
        }
        else {
            getPlayerJournalProgress(player).clearCreature(creatureType);
            RiftMessages.WRAPPER.sendToAll(new RiftJournalEditOne(creatureType, false));
        }
    }

    public static void unlockAllEntries(EntityPlayer player) {
        if (player.world.isRemote) {
            getPlayerJournalProgress(player).unlockAllEntries();
            RiftMessages.WRAPPER.sendToServer(new RiftJournalEditAll(true));
        }
        else {
            getPlayerJournalProgress(player).unlockAllEntries();
            RiftMessages.WRAPPER.sendToAll(new RiftJournalEditAll(true));
        }
    }

    public static void resetEntries(EntityPlayer player) {
        if (player.world.isRemote) {
            getPlayerJournalProgress(player).resetEntries();
            RiftMessages.WRAPPER.sendToServer(new RiftJournalEditAll(false));
        }
        else {
            getPlayerJournalProgress(player).resetEntries();
            RiftMessages.WRAPPER.sendToAll(new RiftJournalEditAll(false));
        }
    }
}
