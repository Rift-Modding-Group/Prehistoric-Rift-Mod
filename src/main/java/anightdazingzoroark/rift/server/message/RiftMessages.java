package anightdazingzoroark.rift.server.message;

import net.ilexiconn.llibrary.server.network.NetworkWrapper;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class RiftMessages {
    @NetworkWrapper({RiftChangeCreatureFromMenu.class, RiftChangeInventoryFromMenu.class, RiftManageCanUseRightClick.class, RiftMountControl.class, RiftOpenInventoryFromMenu.class, RiftStartRiding.class})
    public static SimpleNetworkWrapper WRAPPER;
}