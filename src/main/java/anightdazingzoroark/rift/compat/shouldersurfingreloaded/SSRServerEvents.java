package anightdazingzoroark.rift.compat.shouldersurfingreloaded;

import anightdazingzoroark.rift.RiftUtil;
import anightdazingzoroark.rift.compat.shouldersurfingreloaded.messages.SSRCompatMessages;
import anightdazingzoroark.rift.compat.shouldersurfingreloaded.messages.SSRMountControl;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import anightdazingzoroark.rift.server.entity.RiftEntityProperties;
import anightdazingzoroark.rift.server.events.RiftMouseHoldEvent;
import anightdazingzoroark.rift.server.message.RiftManageCanUseClick;
import anightdazingzoroark.rift.server.message.RiftMessages;
import anightdazingzoroark.rift.server.message.RiftMountControl;
import com.teamderpy.shouldersurfing.client.ShoulderInstance;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.Sys;

public class SSRServerEvents {
    @SubscribeEvent(receiveCanceled = true)
    public void mouseUse(RiftMouseHoldEvent event) {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        EntityPlayer player = Minecraft.getMinecraft().player;
        Item heldItem = player.getHeldItemMainhand().getItem();
        RiftEntityProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(player, RiftEntityProperties.class);

        if (player.getRidingEntity() instanceof RiftCreature && Loader.isModLoaded(SSRCompat.SSR_MOD_ID)) {
            RiftCreature creature = (RiftCreature) player.getRidingEntity();
            if (ShoulderInstance.getInstance().doShoulderSurfing()) {
                //detect left click
                if (!RiftUtil.checkInMountItemWhitelist(heldItem) && event.getMouseButton() == 0) {
                    Entity toBeAttacked = SSRCompatUtils.getEntities(creature.attackWidth * (64D/39D)).entityHit;
                    if (creature.hasLeftClickChargeBar()) {
                        if (creature.getLeftClickCooldown() == 0) {
                            System.out.println("hello");
                            if (!event.isReleased()) properties.leftClickFill++;
                            else {
                                if (toBeAttacked != null) {
                                    if (toBeAttacked instanceof EntityLivingBase) {
                                        int targetId = toBeAttacked.getEntityId();
                                        SSRCompatMessages.SSR_COMPAT_WRAPPER.sendToServer(new SSRMountControl(creature, targetId, 0, properties.leftClickFill));
                                    }
                                }
                                else SSRCompatMessages.SSR_COMPAT_WRAPPER.sendToServer(new SSRMountControl(creature, -1, 0, properties.leftClickFill));
                                properties.leftClickFill = 0;
                            }
                        }
                    }
                    else {
                        if (event.getTicks() <= 10) {
                            if (toBeAttacked != null) {
                                if (toBeAttacked instanceof EntityLivingBase) {
                                    int targetId = toBeAttacked.getEntityId();
                                    SSRCompatMessages.SSR_COMPAT_WRAPPER.sendToServer(new SSRMountControl(creature, targetId, 0));
                                }
                            }
                            else SSRCompatMessages.SSR_COMPAT_WRAPPER.sendToServer(new SSRMountControl(creature, -1, 0));
                        }
                    }
                }
                //detect right click
                else if (!RiftUtil.checkInMountItemWhitelist(heldItem) && !(heldItem instanceof ItemFood) && event.getMouseButton() == 1) {
                    if (creature.getRightClickCooldown() == 0) {
                        if (!event.isReleased()) {
                            properties.rightClickFill++;
                            properties.rCTrigger = true;
                        }
                        else if (event.isReleased() && !creature.canUseRightClick()) {
                            RiftMessages.WRAPPER.sendToServer(new RiftManageCanUseClick(creature, 1, true));
                            properties.rightClickFill = 0;
                        }
                        else if (event.isReleased() && properties.rCTrigger) {
                            RiftMessages.WRAPPER.sendToServer(new RiftMountControl(creature, 1, properties.rightClickFill));
                            properties.rightClickFill = 0;
                            properties.rCTrigger = true;
                        }
                        else if (event.isReleased()) properties.rightClickFill = 0;
                    }
                }
            }
            else {
                //detect left click
                if (!RiftUtil.checkInMountItemWhitelist(heldItem) && event.getMouseButton() == 0) {
                    if (event.getTicks() <= 10) RiftMessages.WRAPPER.sendToServer(new RiftMountControl(creature, 0));
                }
                //detect right click
                else if (!RiftUtil.checkInMountItemWhitelist(heldItem) && !(heldItem instanceof ItemFood) && event.getMouseButton() == 1) {
                    if (creature.getRightClickCooldown() == 0) {
                        if (!event.isReleased()) {
                            properties.rightClickFill++;
                            properties.rCTrigger = true;
                        }
                        else if (event.isReleased() && !creature.canUseRightClick()) {
                            RiftMessages.WRAPPER.sendToServer(new RiftManageCanUseClick(creature, 1, true));
                            properties.rightClickFill = 0;
                        }
                        else if (event.isReleased() && properties.rCTrigger) {
                            RiftMessages.WRAPPER.sendToServer(new RiftMountControl(creature, 1, properties.rightClickFill));
                            properties.rightClickFill = 0;
                            properties.rCTrigger = true;
                        }
                        else if (event.isReleased()) {
                            properties.rightClickFill = 0;
                        }
                    }
                }
            }
        }
    }
}
