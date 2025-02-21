package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftInitialize;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class RiftMessages {
    public static SimpleNetworkWrapper WRAPPER;

    public static void registerMessages() {
        WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(RiftInitialize.MODID);

        int id = 0;
        WRAPPER.registerMessage(RiftApatosaurusManagePassengers.Handler.class, RiftApatosaurusManagePassengers.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftChangeCreatureFromMenu.Handler.class, RiftChangeCreatureFromMenu.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftChangePartyOrBoxOrder.Handler.class, RiftChangePartyOrBoxOrder.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftChangePartyOrBoxOrder.Handler.class, RiftChangePartyOrBoxOrder.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftChangeInventoryFromMenu.Handler.class, RiftChangeInventoryFromMenu.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftChangeInventoryFromMenu.Handler.class, RiftChangeInventoryFromMenu.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftChangeWeaponInvFromMenu.Handler.class, RiftChangeWeaponInvFromMenu.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftChangeWeaponInvFromMenu.Handler.class, RiftChangeWeaponInvFromMenu.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftForceChangePos.Handler.class, RiftForceChangePos.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftGrabberTargeting.Handler.class, RiftGrabberTargeting.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftHoverChangeControl.Handler.class, RiftHoverChangeControl.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftIncrementControlUse.Handler.class, RiftIncrementControlUse.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftJournalEditOne.Handler.class, RiftJournalEditOne.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftJournalEditOne.Handler.class, RiftJournalEditOne.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftLaunchLWeaponProjectile.Handler.class, RiftLaunchLWeaponProjectile.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftManageCanUseControl.Handler.class, RiftManageCanUseControl.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftDeployPartyMem.Handler.class, RiftDeployPartyMem.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftDeployPartyMem.Handler.class, RiftDeployPartyMem.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftManageUtilizingControl.Handler.class, RiftManageUtilizingControl.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftMountControl.Handler.class, RiftMountControl.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftMultipartInteract.Handler.class, RiftMultipartInteract.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftOpenInventoryFromMenu.Handler.class, RiftOpenInventoryFromMenu.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftOpenWeaponInventory.Handler.class, RiftOpenWeaponInventory.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftOpenPopupFromRadial.Handler.class, RiftOpenPopupFromRadial.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftSarcosuchusSpinTargeting.Handler.class, RiftSarcosuchusSpinTargeting.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftSetGrabTarget.Handler.class, RiftSetGrabTarget.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftSetCanWanderHarvest.Handler.class, RiftSetCanWanderHarvest.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftSpawnChestDetectParticle.Handler.class, RiftSpawnChestDetectParticle.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftSpawnDetectParticle.Handler.class, RiftSpawnDetectParticle.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftStartRiding.Handler.class, RiftStartRiding.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftSetWorkstation.Handler.class, RiftSetWorkstation.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftTeleportPartyMemToPlayer.Handler.class, RiftTeleportPartyMemToPlayer.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftChangeNameFromBox.Handler.class, RiftChangeNameFromBox.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftRemoveCreatureFromBox.Handler.class, RiftRemoveCreatureFromBox.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftRemoveCreatureFromBox.Handler.class, RiftRemoveCreatureFromBox.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftRemoveAfterSendToBox.Handler.class, RiftRemoveAfterSendToBox.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftRemoveAfterSendToBox.Handler.class, RiftRemoveAfterSendToBox.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftAddToParty.Handler.class, RiftAddToParty.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftAddToParty.Handler.class, RiftAddToParty.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftUpdatePlayerTamedCreatures.Handler.class, RiftUpdatePlayerTamedCreatures.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftChangeBoxDeployedOrder.Handler.class, RiftChangeBoxDeployedOrder.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftChangeBoxDeployedOrder.Handler.class, RiftChangeBoxDeployedOrder.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftDropPartyMemberInventory.Handler.class, RiftDropPartyMemberInventory.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftDropCreatureBoxDeployedMemberInventory.Handler.class, RiftDropCreatureBoxDeployedMemberInventory.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftSetTurretMode.Handler.class, RiftSetTurretMode.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftChangeTurretTargetingFromMenu.Handler.class, RiftChangeTurretTargetingFromMenu.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftUpdatePlayerJournalProgress.Handler.class, RiftUpdatePlayerJournalProgress.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftJournalEditAll.Handler.class, RiftJournalEditAll.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftJournalEditAll.Handler.class, RiftJournalEditAll.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftUpdateNonPotionEffects.Handler.class, RiftUpdateNonPotionEffects.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftSetBleeding.Handler.class, RiftSetBleeding.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftSetBleeding.Handler.class, RiftSetBleeding.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftManageBleeding.Handler.class, RiftManageBleeding.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftSetBolaCaptured.Handler.class, RiftSetBolaCaptured.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftSetBolaCaptured.Handler.class, RiftSetBolaCaptured.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftManageBolaCaptured.Handler.class, RiftManageBolaCaptured.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftSetCaptured.Handler.class, RiftSetCaptured.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftSetCaptured.Handler.class, RiftSetCaptured.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftSetRiding.Handler.class, RiftSetRiding.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftSetRiding.Handler.class, RiftSetRiding.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftUpgradePlayerParty.Handler.class, RiftUpgradePlayerParty.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftUpgradePlayerParty.Handler.class, RiftUpgradePlayerParty.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftSetPartyLastSelected.Handler.class, RiftSetPartyLastSelected.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftSetPartyLastSelected.Handler.class, RiftSetPartyLastSelected.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftUpgradePlayerBox.Handler.class, RiftUpgradePlayerBox.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftUpgradePlayerBox.Handler.class, RiftUpgradePlayerBox.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftOpenCreatureBoxMenu.Handler.class, RiftOpenCreatureBoxMenu.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftOpenCreatureBoxNoCreaturesMenu.Handler.class, RiftOpenCreatureBoxNoCreaturesMenu.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftModifyPlayerCreature.Handler.class, RiftModifyPlayerCreature.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftModifyPlayerCreature.Handler.class, RiftModifyPlayerCreature.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftCreatureBoxSetLastOpenedTime.Handler.class, RiftCreatureBoxSetLastOpenedTime.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftCreatureBoxSetLastOpenedTime.Handler.class, RiftCreatureBoxSetLastOpenedTime.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftUpdatePartyDeployed.Handler.class, RiftUpdatePartyDeployed.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftUpdatePartyDeployed.Handler.class, RiftUpdatePartyDeployed.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftRemoveCreature.Handler.class, RiftRemoveCreature.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftRemoveCreature.Handler.class, RiftRemoveCreature.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftUpdateBoxDeployed.Handler.class, RiftUpdateBoxDeployed.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftUpdateBoxDeployed.Handler.class, RiftUpdateBoxDeployed.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftPartySetLastOpenedTime.Handler.class, RiftPartySetLastOpenedTime.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftPartySetLastOpenedTime.Handler.class, RiftPartySetLastOpenedTime.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftForceSyncPartyNBT.Handler.class, RiftForceSyncPartyNBT.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftForceSyncPartyNBT.Handler.class, RiftForceSyncPartyNBT.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftForceSyncBoxNBT.Handler.class, RiftForceSyncBoxNBT.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftForceSyncBoxNBT.Handler.class, RiftForceSyncBoxNBT.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftForceSyncPartySizeLevel.Handler.class, RiftForceSyncPartySizeLevel.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftForceSyncPartySizeLevel.Handler.class, RiftForceSyncPartySizeLevel.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftForceSyncBoxSizeLevel.Handler.class, RiftForceSyncBoxSizeLevel.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftForceSyncBoxSizeLevel.Handler.class, RiftForceSyncBoxSizeLevel.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftForceSyncLastSelected.Handler.class, RiftForceSyncLastSelected.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftForceSyncLastSelected.Handler.class, RiftForceSyncLastSelected.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftForceSyncPartyLastOpenedTime.Handler.class, RiftForceSyncPartyLastOpenedTime.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftForceSyncPartyLastOpenedTime.Handler.class, RiftForceSyncPartyLastOpenedTime.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftForceSyncBoxLastOpenedTime.Handler.class, RiftForceSyncBoxLastOpenedTime.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftForceSyncBoxLastOpenedTime.Handler.class, RiftForceSyncBoxLastOpenedTime.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftAddToBox.Handler.class, RiftAddToBox.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftAddToBox.Handler.class, RiftAddToBox.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftAddToBoxNBT.Handler.class, RiftAddToBoxNBT.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftAddToBoxNBT.Handler.class, RiftAddToBoxNBT.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(RiftManualUseMove.Handler.class, RiftManualUseMove.class, id++, Side.SERVER);
        WRAPPER.registerMessage(RiftManualUseLargeWeapon.Handler.class, RiftManualUseLargeWeapon.class, id++, Side.SERVER);
    }
}