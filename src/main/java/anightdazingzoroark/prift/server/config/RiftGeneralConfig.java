package anightdazingzoroark.prift.server.config;

import anightdazingzoroark.prift.RiftInitialize;
import net.minecraftforge.common.config.Config;

@SuppressWarnings("WeakerAccess")
@Config(modid = RiftInitialize.MODID, name = "prift/general")
public class RiftGeneralConfig {
    @Config.Name("Creature Settings")
    @Config.Comment("Settings specific to creatures from the mod")
    public static final Creatures creatures = new Creatures();

    @Config.Name("Other Settings")
    @Config.Comment("Miscellaneous settings")
    public static final Other other = new Other();

    public static class Creatures {
        @Config.Name("Mobs killed by wild creatures don't drop loot")
        @Config.Comment({
                "This is here to ensure that lag from frequent item",
                "drops from mobs killed by wild creatures won't occur.",
                "Disable at your own risk."
        })
        public boolean creatureKillNoLoot = true;
    }

    public static class Other {
        @Config.Name("Show Discord join message when opening a world")
        @Config.Comment({
                "Send to anyone who joins the world in chat a message",
                "linking to the official Rift Modding Discord server.",
                "Setting this to false will make me sad :("
        })
        public boolean showJoinDiscordMessage = true;
    }
}
