package anightdazingzoroark.prift.client;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.entity.creature.Anomalocaris;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftOpenInventoryFromMenu;
import anightdazingzoroark.prift.server.message.RiftOpenWeaponInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.opengl.GL11;

public class ClientEvents {
    //set cam to 3rd person when ridin a creature
    @SubscribeEvent
    public void onEntityMount(EntityMountEvent event) {
        if (event.getEntityBeingMounted() instanceof RiftCreature && event.getWorldObj().isRemote && event.getEntityMounting() == Minecraft.getMinecraft().player) {
            RiftCreature creature = (RiftCreature)event.getEntityBeingMounted();
            if (creature.isTamed() && creature.isOwner(Minecraft.getMinecraft().player)) {
                if (event.isDismounting()) {
                    Minecraft.getMinecraft().gameSettings.thirdPersonView = RiftInitialize.PROXY.getPreviousViewType();
                }
                else {
                    RiftInitialize.PROXY.setPreviousViewType(Minecraft.getMinecraft().gameSettings.thirdPersonView);
                    Minecraft.getMinecraft().gameSettings.thirdPersonView = 1;
                    RiftInitialize.PROXY.set3rdPersonView(2);
                }
            }
        }
    }

    //open creature inventory while riding
    @SubscribeEvent
    public void openInvWhileRiding(GuiOpenEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (event.getGui() instanceof GuiInventory && player.isRiding() && player.getRidingEntity() instanceof RiftCreature) {
            RiftMessages.WRAPPER.sendToServer(new RiftOpenInventoryFromMenu(player.getRidingEntity().getEntityId()));
            event.setCanceled(true);
        }
        if (event.getGui() instanceof GuiInventory && player.isRiding() && player.getRidingEntity() instanceof RiftLargeWeapon) {
            RiftMessages.WRAPPER.sendToServer(new RiftOpenWeaponInventory((RiftLargeWeapon)player.getRidingEntity()));
            event.setCanceled(true);
        }
    }

    //make player invisible when anomalocaris is invisible
    @SubscribeEvent
    public void onPlayerRender(RenderPlayerEvent.Pre event) {
        if (event.getEntityPlayer().isRiding()) {
            if (event.getEntityPlayer().getRidingEntity() instanceof Anomalocaris) {
                Anomalocaris anomalocaris = (Anomalocaris)event.getEntityPlayer().getRidingEntity();

                GL11.glPushMatrix();
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, anomalocaris.isUsingInvisibility() ? 0.25f : 1f);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glPopMatrix();
            }
        }
    }

    //open journal when pressing the journal button
    //player.openGui(RiftInitialize.instance, ServerProxy.GUI_CREATURE_INVENTORY, world, message.creatureId, 0, 0);
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void openJournal(InputEvent.KeyInputEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (RiftControls.openJournal.isKeyDown()) {
            player.openGui(RiftInitialize.instance, ServerProxy.GUI_JOURNAL, player.world, 0, 0, 0);
        }
    }
}
