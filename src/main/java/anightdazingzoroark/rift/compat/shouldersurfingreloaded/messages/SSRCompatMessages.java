package anightdazingzoroark.rift.compat.shouldersurfingreloaded.messages;

import anightdazingzoroark.rift.RiftInitialize;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class SSRCompatMessages {
    private static int id = 0;
    public static SimpleNetworkWrapper SSR_COMPAT_WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(RiftInitialize.MODID+"_ssr_compat");

    public static void registerMessages() {
        SSR_COMPAT_WRAPPER.registerMessage(SSRMountControl.Handler.class, SSRMountControl.class, id++, Side.SERVER);
    }
}
