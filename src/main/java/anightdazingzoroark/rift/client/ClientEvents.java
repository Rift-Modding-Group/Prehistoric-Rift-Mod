package anightdazingzoroark.rift.client;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import net.minecraft.client.Minecraft;
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
}
