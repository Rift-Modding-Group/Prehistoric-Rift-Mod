package anightdazingzoroark.prift.server.properties;

import anightdazingzoroark.prift.propertySystem.registry.PropertyRegistry;
import anightdazingzoroark.prift.server.properties.journalProgress.JournalProgressProperties;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxProperties;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyProperties;
import net.minecraft.entity.player.EntityPlayer;

public class RiftPropertyRegistry {
    public static final String PLAYER_PARTY = "playerParty";
    public static final String JOURNAL_PROGRESS = "journalProgress";
    public static final String PLAYER_CREATURE_BOX = "playerCreatureBox";

    public static void register() {
        PropertyRegistry.register(PLAYER_PARTY, EntityPlayer.class, PlayerPartyProperties.class);
        PropertyRegistry.register(JOURNAL_PROGRESS, EntityPlayer.class, JournalProgressProperties.class);
        PropertyRegistry.register(PLAYER_CREATURE_BOX, EntityPlayer.class, PlayerCreatureBoxProperties.class);
    }
}
