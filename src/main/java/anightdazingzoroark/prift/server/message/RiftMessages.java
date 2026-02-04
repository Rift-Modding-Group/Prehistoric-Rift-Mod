package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.message.RiftLibMessageSide;
import anightdazingzoroark.riftlib.message.RiftLibMessageWrapper;

public class RiftMessages {
    public static RiftLibMessageWrapper<RiftLibMessage, RiftLibMessage> WRAPPER;

    public static void registerMessages() {
        WRAPPER = new RiftLibMessageWrapper<>(RiftInitialize.MODID);

        WRAPPER.registerMessage(RiftApatosaurusManagePassengers.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftChangeCreatureFromMenu.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftChangeWeaponInvFromMenu.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftForceChangePos.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftSetEntityGrabbed.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftGrabbedEntitySetPos.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftHoverChangeControl.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftIncrementControlUse.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftLaunchLWeaponProjectile.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftCanUseMoveTriggerButton.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftDeployPartyMem.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftManageUtilizingControl.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftOpenInventoryFromMenu.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftOpenWeaponInventory.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftSetGrabTarget.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftSetCanWanderHarvest.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftSpawnChestDetectParticle.class, RiftLibMessageSide.CLIENT);
        WRAPPER.registerMessage(RiftSpawnDetectParticle.class, RiftLibMessageSide.CLIENT);
        WRAPPER.registerMessage(RiftStartRiding.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftSetWorkstation.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftTeleportPartyMemToPlayer.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftUpdatePlayerTamedCreatures.class, RiftLibMessageSide.CLIENT);
        WRAPPER.registerMessage(RiftSetTurretMode.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftChangeTurretTargetingFromMenu.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftUpdatePlayerJournalProgress.class, RiftLibMessageSide.CLIENT);
        WRAPPER.registerMessage(RiftUpdateNonPotionEffects.class, RiftLibMessageSide.CLIENT);
        WRAPPER.registerMessage(RiftSetBleeding.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftStopBleeding.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftSetBolaCaptured.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftResetBolaCaptured.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftSetRiding.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftForceSyncPartyNBT.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftForceSyncBoxNBT.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftManualUseMove.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftManualUseLargeWeapon.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftSetSprinting.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftShowParticlesOnClient.class, RiftLibMessageSide.CLIENT);
        WRAPPER.registerMessage(RiftManageBlockBreakControl.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftUpdatePartyDeployed.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftAddToParty.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftSyncJournal.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftJournalEditOne.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftJournalEditAll.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftForceSyncSelectedPartyPosFromOverlay.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftSetSelectedPartyPosFromOverlay.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftUpdateIndividualPartyCreatureClient.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftUpdateIndividualPartyCreatureHealthClient.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftUpdateIndividualPartyCreatureEnergyClient.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftUpdateIndividualPartyCreatureXPClient.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftUpdateIndividualPartyCreatureServer.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftForceSyncLastOpenedBox.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftSetLastOpenedBox.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftAddToBox.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftChangeCreatureBoxName.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftChangeSelectedCreatureName.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftReleaseSelectedCreature.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftSwapCreaturePositions.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftForceUpdateCreatureBoxDeployed.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftUpdateIndividualBoxDeployedCreatureClient.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftUpdateIndividualBoxDeployedCreatureServer.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftDropSelectedInventory.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftUpdateAllCreatureBoxDeployedMems.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftForceSyncBoxDeployedNBT.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftCreatureBoxSetLastOpenedTime.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftAddCreatureBoxData.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftRemoveCreatureBoxData.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftSetHypnotized.class, RiftLibMessageSide.BOTH);
    }
}