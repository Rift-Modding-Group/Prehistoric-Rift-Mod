package anightdazingzoroark.prift.client;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftOpenInventoryFromMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
    }
}
