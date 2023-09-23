package anightdazingzoroark.rift.compat.shouldersurfingreloaded;

import anightdazingzoroark.rift.compat.shouldersurfingreloaded.messages.SSRCompatMessages;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;

public class SSRCompat {
    public static final String SSR_MOD_ID = "shouldersurfing";

    public static void ssrPreInit() {
        if (Loader.isModLoaded(SSR_MOD_ID)) {
            System.out.println("shoulder surf test");
            SSRCompatMessages.registerMessages();
        }
    }

    public static void ssrInit() {
        if (Loader.isModLoaded(SSR_MOD_ID)) {
            MinecraftForge.EVENT_BUS.register(new SSRServerEvents());
        }
    }
}