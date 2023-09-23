package anightdazingzoroark.rift.compat.shouldersurfingreloaded;

import anightdazingzoroark.rift.RiftUtil;
import anightdazingzoroark.rift.compat.shouldersurfingreloaded.messages.SSRCompatMessages;
import anightdazingzoroark.rift.compat.shouldersurfingreloaded.messages.SSRMountControl;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import anightdazingzoroark.rift.server.events.RiftMouseHoldEvent;
import anightdazingzoroark.rift.server.message.RiftManageCanUseRightClick;
import anightdazingzoroark.rift.server.message.RiftMessages;
import anightdazingzoroark.rift.server.message.RiftMountControl;
import com.teamderpy.shouldersurfing.client.ShoulderInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.Sys;

public class SSRServerEvents {
    private int rightClickFill = 0;
    private boolean rCTrigger = true;

    @SubscribeEvent(receiveCanceled = true)
    public void mouseUse(RiftMouseHoldEvent event) {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        EntityPlayer player = Minecraft.getMinecraft().player;
        Item heldItem = player.getHeldItemMainhand().getItem();

        if (player.getRidingEntity() instanceof RiftCreature && Loader.isModLoaded(SSRCompat.SSR_MOD_ID)) {
            RiftCreature creature = (RiftCreature) player.getRidingEntity();
            if (ShoulderInstance.getInstance().doShoulderSurfing()) {
                //detect left click
                if (!RiftUtil.checkInMountItemWhitelist(heldItem) && event.getMouseButton() == 0) {
                    if (event.getTicks() <= 10) {
                        if (SSRCompatUtils.getEntities(8D) != null) {
                            if (SSRCompatUtils.getEntities(8D).entityHit instanceof EntityLivingBase) {
                                int targetId = SSRCompatUtils.getEntities(8D).entityHit.getEntityId();
                                SSRCompatMessages.SSR_COMPAT_WRAPPER.sendToServer(new SSRMountControl(creature, targetId, 0));
                                KeyBinding.setKeyBindState(settings.keyBindAttack.getKeyCode(), false);
                            }
                        }
                        else {
                            SSRCompatMessages.SSR_COMPAT_WRAPPER.sendToServer(new SSRMountControl(creature, -1, 0));
                            KeyBinding.setKeyBindState(settings.keyBindAttack.getKeyCode(), false);
                        }
                    }
                }
                //detect right click
                else if (!RiftUtil.checkInMountItemWhitelist(heldItem) && !(heldItem instanceof ItemFood) && event.getMouseButton() == 1 && creature.getRightClickCooldown() == 0) {
                    if (!event.isReleased()) {
                        rightClickFill++;
                        rCTrigger = true;
                    }
                    else if (event.isReleased() && !creature.canUseRightClick()) {
                        RiftMessages.WRAPPER.sendToServer(new RiftManageCanUseRightClick(creature, true));
                        rightClickFill = 0;
                    }
                    else if (event.isReleased() && rCTrigger) {
                        RiftMessages.WRAPPER.sendToServer(new RiftMountControl(creature, 1, rightClickFill));
                        rightClickFill = 0;
                        rCTrigger = false;
                    }
                    else if (event.isReleased()) {
                        rightClickFill = 0;
                    }
                    KeyBinding.setKeyBindState(settings.keyBindUseItem.getKeyCode(), false);
                }
            }
            else {
                //detect left click
                if (!RiftUtil.checkInMountItemWhitelist(heldItem) && event.getMouseButton() == 0) {
                    if (event.getTicks() <= 10) {
                        RiftMessages.WRAPPER.sendToServer(new RiftMountControl(creature, 0));
                        KeyBinding.setKeyBindState(settings.keyBindAttack.getKeyCode(), false);
                    }
                }
                //detect right click
                else if (!RiftUtil.checkInMountItemWhitelist(heldItem) && !(heldItem instanceof ItemFood) && event.getMouseButton() == 1 && creature.getRightClickCooldown() == 0) {
                    if (!event.isReleased()) {
                        rightClickFill++;
                        rCTrigger = true;
                    }
                    else if (event.isReleased() && !creature.canUseRightClick()) {
                        RiftMessages.WRAPPER.sendToServer(new RiftManageCanUseRightClick(creature, true));
                        rightClickFill = 0;
                    }
                    else if (event.isReleased() && rCTrigger) {
                        RiftMessages.WRAPPER.sendToServer(new RiftMountControl(creature, 1, rightClickFill));
                        rightClickFill = 0;
                        rCTrigger = false;
                    }
                    else if (event.isReleased()) {
                        rightClickFill = 0;
                    }
                    KeyBinding.setKeyBindState(settings.keyBindUseItem.getKeyCode(), false);
                }
            }
        }
    }
}
