package anightdazingzoroark.rift.server.message;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class RiftMessages {
    private static int id = 0;
    public static SimpleNetworkWrapper NETWORK_WRAPPER;

    public RiftMessages() {}

    public static void registerMessages(String channelName) {
        NETWORK_WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(channelName);
        registerMessages();
    }

    public static void registerMessages() {
        NETWORK_WRAPPER.registerMessage(RiftChangeCreatureFromMenu.Handler.class, RiftChangeCreatureFromMenu.class, id++, Side.SERVER);
    }
}
