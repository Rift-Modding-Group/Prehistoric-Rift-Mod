package anightdazingzoroark.rift.server;

import anightdazingzoroark.rift.RiftConfig;
import anightdazingzoroark.rift.RiftUtil;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import anightdazingzoroark.rift.server.message.RiftMessages;
import anightdazingzoroark.rift.server.message.RiftMountControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.util.ArrayList;
import java.util.List;

public class ServerEvents {
    //for controlling when u use attacks or abilities while riding creatures
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void whileRidingCreature(InputEvent.MouseInputEvent event) {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        EntityPlayer player = Minecraft.getMinecraft().player;

        if (player.getRidingEntity() instanceof RiftCreature && !checkInItemWhitelist(player.getHeldItemMainhand().getItem()) && settings.keyBindAttack.isPressed()) {
            RiftMessages.WRAPPER.sendToServer(new RiftMountControl((RiftCreature) player.getRidingEntity(), 0));
            KeyBinding.setKeyBindState(settings.keyBindAttack.getKeyCode(), false);
        }
        else if (player.getRidingEntity() instanceof RiftCreature && !checkInItemWhitelist(player.getHeldItemMainhand().getItem()) && settings.keyBindUseItem.isPressed()) {
            RiftMessages.WRAPPER.sendToServer(new RiftMountControl((RiftCreature) player.getRidingEntity(), 1));
            KeyBinding.setKeyBindState(settings.keyBindUseItem.getKeyCode(), false);
        }
    }

    //when a tamed creature gets attacked by a wild creature of the same type, the damage they received is halved
    @SubscribeEvent
    public void reduceDamageTamed(LivingHurtEvent event) {
        if (event.getEntity() instanceof RiftCreature && event.getSource().getTrueSource() instanceof RiftCreature) {
            RiftCreature creature = (RiftCreature) event.getEntity();
            RiftCreature attacker = (RiftCreature) event.getSource().getTrueSource();
            if (creature.isTamed() && !attacker.isTamed() && creature.creatureType == attacker.creatureType) {
                event.setAmount(event.getAmount() / 2);
            }
        }
    }

    //to reduce potential lag, mobs killed by wild creatures will not drop items
    @SubscribeEvent
    public void stopMobDrops(LivingDropsEvent event) {
        if (event.getSource().getTrueSource() instanceof RiftCreature) {
            RiftCreature attacker = (RiftCreature) event.getSource().getTrueSource();
            if (!attacker.isTamed()) {
                event.setCanceled(true);
            }
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