package anightdazingzoroark.prift.server.capabilities.playerJournalProgress;

import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.message.*;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerJournalProgressHelper {
    public static IPlayerJournalProgress getPlayerJournalProgress(EntityPlayer player) {
        if (player == null) return null;
        return player.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
    }

    public static Map<RiftCreatureType, Boolean> getUnlockedCreatures(EntityPlayer player) {
        if (player.world.isRemote) RiftMessages.WRAPPER.sendToServer(new RiftSyncJournal(player));
        return getPlayerJournalProgress(player).getEncounteredCreatures();
    }

    public static List<RiftCreatureType.CreatureCategory> getUnlockedCategories(EntityPlayer player) {
        if (player.world.isRemote) RiftMessages.WRAPPER.sendToServer(new RiftSyncJournal(player));
        return getPlayerJournalProgress(player).getUnlockedCategories();
    }

    public static void discoverCreature(EntityPlayer player, RiftCreatureType creatureType) {
        RiftMessages.WRAPPER.sendToServer(new RiftJournalEditOne(player, creatureType, true, false));
    }

    public static void unlockCreature(EntityPlayer player, RiftCreatureType creatureType) {
        RiftMessages.WRAPPER.sendToServer(new RiftJournalEditOne(player, creatureType, true));
    }

    public static void clearCreature(EntityPlayer player, RiftCreatureType creatureType) {
        RiftMessages.WRAPPER.sendToServer(new RiftJournalEditOne(player, creatureType, false));
    }

    public static void unlockAllEntries(EntityPlayer player) {
        RiftMessages.WRAPPER.sendToServer(new RiftJournalEditAll(player, true));
    }

    public static void resetEntries(EntityPlayer player) {
        RiftMessages.WRAPPER.sendToServer(new RiftJournalEditAll(player, false));
    }

    public static Map<RiftCreatureType, Boolean> getUnlockedEntriesFromCategory(EntityPlayer player, RiftCreatureType.CreatureCategory category) {
        Map<RiftCreatureType, Boolean> toReturn = new HashMap<>();
        Map<RiftCreatureType, Boolean> unlockedCreatures = getUnlockedCreatures(player);

        if (category == RiftCreatureType.CreatureCategory.ALL) return unlockedCreatures;

        if (!unlockedCreatures.isEmpty()) {
            for (Map.Entry<RiftCreatureType, Boolean> entry: unlockedCreatures.entrySet()) {
                if (entry.getKey().getCreatureCategory().equals(category)) toReturn.put(entry.getKey(), entry.getValue());
            }
        }

        return toReturn;
    }
}
