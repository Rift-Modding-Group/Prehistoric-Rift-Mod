package anightdazingzoroark.rift.server.message;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class RiftMessages {
    private static int id = 0;
    public static SimpleNetworkWrapper RADIAL_MENU = NetworkRegistry.INSTANCE.newSimpleChannel("radialMenu");
    public static SimpleNetworkWrapper OPEN_INVENTORY = NetworkRegistry.INSTANCE.newSimpleChannel("openInventory");

    public RiftMessages() {}

    public static void registerMessages() {
        RADIAL_MENU.registerMessage(RiftChangeCreatureFromMenu.Handler.class, RiftChangeCreatureFromMenu.class, id++, Side.SERVER);
        OPEN_INVENTORY.registerMessage(RiftCreatureInventoryFromMenu.Handler.class, RiftCreatureInventoryFromMenu.class, id++, Side.SERVER);
    }
}
