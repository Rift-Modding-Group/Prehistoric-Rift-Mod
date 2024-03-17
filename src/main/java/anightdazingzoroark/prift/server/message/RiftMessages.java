package anightdazingzoroark.prift.server.message;

import net.ilexiconn.llibrary.server.network.NetworkWrapper;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class RiftMessages {
    @NetworkWrapper({RiftApatosaurusManagePassengers.class, RiftChangeCreatureFromMenu.class, RiftChangeHomePosFromMenu.class, RiftChangeCreatureName.class, RiftChangeInventoryFromMenu.class, RiftChangeWeaponInvFromMenu.class, RiftClearHomePosFromPopup.class, RiftGrabberTargeting.class, RiftHoverChangeControl.class, RiftIncrementControlUse.class, RiftLaunchLWeaponProjectile.class, RiftManageCanUseControl.class, RiftManageUtilizingControl.class, RiftMountControl.class, RiftMultipartInteract.class, RiftOnHitMultipart.class, RiftOpenInventoryFromMenu.class, RiftOpenWeaponInventory.class, RiftOpenPopupFromRadial.class, RiftSarcosuchusSpinTargeting.class, RiftStartRiding.class, RiftManageClaimCreature.class, RiftSetWorkstation.class})
    public static SimpleNetworkWrapper WRAPPER;
}