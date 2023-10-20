package anightdazingzoroark.rift.client;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.RiftUtil;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import anightdazingzoroark.rift.server.entity.RiftEntityProperties;
import anightdazingzoroark.rift.server.message.RiftMessages;
import anightdazingzoroark.rift.server.message.RiftOpenInventoryFromMenu;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

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

    //show bleed particles
//    @SubscribeEvent
//    public void onPostRenderLiving(RenderLivingEvent.Post event) {
//    }

    @SubscribeEvent
    public void forEachTick(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        RiftEntityProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(entity, RiftEntityProperties.class);
        if (entity.world.isRemote) {
            if (properties.isBleeding) {
                double motionY = RiftUtil.randomInRange(-0.75D, -0.25D);
                double f = entity.getRNG().nextFloat() * (entity.getEntityBoundingBox().maxX - entity.getEntityBoundingBox().minX) + entity.getEntityBoundingBox().minX;
                double f1 = entity.getRNG().nextFloat() * (entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) + entity.getEntityBoundingBox().minY;
                double f2 = entity.getRNG().nextFloat() * (entity.getEntityBoundingBox().maxZ - entity.getEntityBoundingBox().minZ) + entity.getEntityBoundingBox().minZ;

                RiftInitialize.PROXY.spawnParticle("bleed", f, f1, f2, 0D, motionY, 0D);

            }
        }
    }
}
