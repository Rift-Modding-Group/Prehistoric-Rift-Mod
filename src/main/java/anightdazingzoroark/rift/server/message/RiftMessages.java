package anightdazingzoroark.rift.server.message;

import net.ilexiconn.llibrary.server.network.NetworkWrapper;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class RiftMessages {
    @NetworkWrapper({RiftChangeCreatureFromMenu.class, RiftChangeHomePosFromMenu.class, RiftChangeCreatureName.class, RiftChangeInventoryFromMenu.class, RiftClearHomePosFromPopup.class, RiftIncrementClickUse.class, RiftManageCanUseClick.class, RiftManageUtilizingClick.class, RiftMountControl.class, RiftOpenInventoryFromMenu.class, RiftOpenPopupFromRadial.class, RiftStartRiding.class, RiftManageClaimCreature.class})
    public static SimpleNetworkWrapper WRAPPER;
}