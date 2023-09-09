package anightdazingzoroark.rift.server;

import anightdazingzoroark.rift.RiftConfig;
import anightdazingzoroark.rift.RiftUtil;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import anightdazingzoroark.rift.server.message.RiftMessages;
import anightdazingzoroark.rift.server.message.RiftMountControl;
import anightdazingzoroark.rift.server.message.RiftOpenInventoryFromMenu;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.util.ArrayList;
import java.util.List;

public class ServerEvents {
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void whileRidingCreature(InputEvent.MouseInputEvent event) {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        EntityPlayer player = Minecraft.getMinecraft().player;

        if (player.getRidingEntity() instanceof RiftCreature && !checkInItemWhitelist(player.getHeldItemMainhand().getItem()) && settings.keyBindAttack.isPressed()) {
            RiftMessages.WRAPPER.sendToServer(new RiftMountControl((RiftCreature) player.getRidingEntity(), 0));
            KeyBinding.setKeyBindState(settings.keyBindAttack.getKeyCode(), false);
        }
        else if (player.getRidingEntity() instanceof RiftCreature && !checkInItemWhitelist(player.getHeldItemMainhand().getItem()) && settings.keyBindUseItem.isPressed()) {
            KeyBinding.setKeyBindState(settings.keyBindUseItem.getKeyCode(), false);
        }
    }

    private boolean checkInItemWhitelist(Item item) {
        List<String> oreDicList = new ArrayList<>();
        List<String> itemList = new ArrayList<>();
        for (String entry : RiftConfig.mountOverrideWhitelistItems) {
            if (entry.contains("oreDic:")) {
                oreDicList.add(entry.replace("oreDic:", ""));
            }
            if (entry.contains("item:")) {
                itemList.add(entry.replace("item:", ""));
            }
        }
        for (String oreDicEntry : oreDicList) {
            if (RiftUtil.itemInOreDicType(item, oreDicEntry)) return true;
        }
        for (String itemEntry : itemList) {
            if (Item.getByNameOrId(itemEntry).equals(item) || (item instanceof ItemFood)) return true;
        }
        return false;
    }
}