package anightdazingzoroark.rift.server.message;

import anightdazingzoroark.rift.RiftInitialize;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class RiftMessages {
    private static int id = 0;
    public static SimpleNetworkWrapper WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(RiftInitialize.MODID);

    public RiftMessages() {}

    public static void registerMessages() {
        WRAPPER.registerMessage(RiftChangeCreatureFromMenu.Handler.class, RiftChangeCreatureFromMenu.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftCreatureInventoryFromMenu.Handler.class, RiftCreatureInventoryFromMenu.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftChangeInventoryFromMenu.Handler.class, RiftChangeInventoryFromMenu.class, id++, Side.SERVER);
    }
}
