package anightdazingzoroark.prift.server.properties.journalProgress;

import anightdazingzoroark.prift.propertySystem.Property;
import anightdazingzoroark.prift.server.properties.RiftPropertyRegistry;
import net.minecraft.entity.player.EntityPlayer;

public class JournalProgressHelper {
    public static JournalProgressProperties getJournalProgress(EntityPlayer player) {
        if (player == null) return null;
        return Property.getProperty(RiftPropertyRegistry.JOURNAL_PROGRESS, player);
    }
}
