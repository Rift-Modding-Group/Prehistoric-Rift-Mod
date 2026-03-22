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
        WRAPPER.registerMessage(RiftSetTurretMode.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftChangeTurretTargetingFromMenu.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftUpdateNonPotionEffects.class, RiftLibMessageSide.CLIENT);
        WRAPPER.registerMessage(RiftSetBleeding.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftStopBleeding.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftSetBolaCaptured.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftResetBolaCaptured.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftSetRiding.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftManualUseMove.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftManualUseLargeWeapon.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftSetSprinting.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftShowParticlesOnClient.class, RiftLibMessageSide.CLIENT);
        WRAPPER.registerMessage(RiftManageBlockBreakControl.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftChangeCreatureBoxName.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftAddCreatureBoxData.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftRemoveCreatureBoxData.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftSetHypnotized.class, RiftLibMessageSide.BOTH);
        WRAPPER.registerMessage(RiftOpenCreatureScreen.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftApplyCreatureSwap.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftChangeQuickSelectPos.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftOpenCreatureBoxUI.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftSetSelectedCreature.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftReleaseCreature.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftChangeCreatureName.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftSetSelectedCreatureMultiple.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftSetRevivalInfoClient.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftSetBoxLastOpenedTime.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftUpdateTileEntityProperty.class, RiftLibMessageSide.SERVER);
        WRAPPER.registerMessage(RiftUpdateTileEntityProperty.class, RiftLibMessageSide.SERVER);
    }
}