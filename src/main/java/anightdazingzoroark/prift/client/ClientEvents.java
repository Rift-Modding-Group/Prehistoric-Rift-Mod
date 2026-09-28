package anightdazingzoroark.prift.client;

import anightdazingzoroark.prift.client.hud.PlayerPartyHUD;
import anightdazingzoroark.prift.client.hud.TameProgressHUD;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureGuiData;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureGuiFactory;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftPartyActionMessage;
import com.cleanroommc.modularui.factory.GuiManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class ClientEvents {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void openRiddenCreatureUI(GuiOpenEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (!(event.getGui() instanceof GuiInventory) || minecraft.player == null) return;
        if (!(minecraft.player.getRidingEntity() instanceof RiftCreature creature)
                || creature.getControllingPassenger() != minecraft.player) return;

        event.setCanceled(true);
        GuiManager.openFromClient(RiftCreatureGuiFactory.INSTANCE, new RiftCreatureGuiData(minecraft.player, creature));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void renderHUD(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null || minecraft.gameSettings.hideGUI) return;

        PlayerPartyHUD.renderPlayerParty(minecraft, event.getResolution().getScaledWidth(), event.getResolution().getScaledHeight());
        if (minecraft.objectMouseOver != null) {
            TameProgressHUD.renderTamingProgress(minecraft, event.getResolution().getScaledWidth(), event.getResolution().getScaledHeight());
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void useKeyInputs(InputEvent.KeyInputEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null) return;

        //---for party management---
        boolean usedControl = false;
        if (RiftControls.SWITCH_PARTY_MEMBER_UP.isPressed()) {
            RiftMessages.WRAPPER.sendToServer(new RiftPartyActionMessage(RiftPartyActionMessage.Action.SELECT_PREVIOUS));
            usedControl = true;
        }
        if (RiftControls.SWITCH_PARTY_MEMBER_DOWN.isPressed()) {
            RiftMessages.WRAPPER.sendToServer(new RiftPartyActionMessage(RiftPartyActionMessage.Action.SELECT_NEXT));
            usedControl = true;
        }
        if (RiftControls.DEPLOY_PARTY_MEMBER.isPressed()) {
            RiftMessages.WRAPPER.sendToServer(new RiftPartyActionMessage(RiftPartyActionMessage.Action.DEPLOY_OR_DISMISS));
            usedControl = true;
        }
        if (usedControl) {
            minecraft.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1f));
        }

        //---for riding controls---
    }
}
