package anightdazingzoroark.rift.server.message;

import net.ilexiconn.llibrary.server.network.NetworkWrapper;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class RiftMessages {
    @NetworkWrapper({RiftChangeCreatureFromMenu.class, RiftChangeHomePosFromMenu.class, RiftChangeInventoryFromMenu.class, RiftIncrementClickUse.class, RiftManageCanUseClick.class, RiftMountControl.class, RiftOpenInventoryFromMenu.class, RiftOpenPopupFromRadial.class, RiftStartRiding.class})
    public static SimpleNetworkWrapper WRAPPER;
}