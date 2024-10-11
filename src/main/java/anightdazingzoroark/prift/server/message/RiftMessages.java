package anightdazingzoroark.prift.server.message;

import net.ilexiconn.llibrary.server.network.NetworkWrapper;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class RiftMessages {
    @NetworkWrapper({RiftApatosaurusManagePassengers.class, RiftChangeCreatureFromMenu.class, RiftChangeHomePosFromMenu.class, RiftChangePartyOrBoxOrder.class, RiftChangeCreatureName.class, RiftChangeInventoryFromMenu.class, RiftChangeVelocity.class, RiftChangeWeaponInvFromMenu.class, RiftClearHomePosFromPopup.class, RiftForceChangePos.class, RiftGrabberTargeting.class, RiftHoverChangeControl.class, RiftIncrementControlUse.class, RiftJournalEdit.class, RiftLaunchLWeaponProjectile.class, RiftManageCanUseControl.class, RiftManagePartyMem.class, RiftManageUtilizingControl.class, RiftMountControl.class, RiftMultipartInteract.class, RiftOnHitMultipart.class, RiftOpenInventoryFromMenu.class, RiftOpenWeaponInventory.class, RiftOpenPopupFromRadial.class, RiftSarcosuchusSpinTargeting.class, RiftSetGrabTarget.class, RiftSetCanWanderHarvest.class, RiftSpawnChestDetectParticle.class, RiftSpawnDetectParticle.class, RiftStartRiding.class, RiftManageClaimCreature.class, RiftSetWorkstation.class, RiftTeleportPartyMemToPlayer.class, RiftChangeNameFromBox.class, RiftRemoveCreatureFromBox.class, RiftRemoveAfterSendToBox.class, RiftAddToParty.class, RiftUpdatePlayerTamedCreatures.class, RiftChangeBoxDeployedOrder.class})
    public static SimpleNetworkWrapper WRAPPER;
}