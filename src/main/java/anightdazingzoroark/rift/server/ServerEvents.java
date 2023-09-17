package anightdazingzoroark.rift.server;

import anightdazingzoroark.rift.RiftConfig;
import anightdazingzoroark.rift.RiftUtil;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import anightdazingzoroark.rift.server.events.RiftMouseHoldEvent;
import anightdazingzoroark.rift.server.message.RiftMessages;
import anightdazingzoroark.rift.server.message.RiftMountControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.Sys;

import java.util.ArrayList;
import java.util.List;

public class ServerEvents {
    //for controlling when u use attacks or abilities while riding creatures
    @SubscribeEvent(receiveCanceled = true)
    public void mouseTest(RiftMouseHoldEvent event) {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        EntityPlayer player = Minecraft.getMinecraft().player;
        Item heldItem = player.getHeldItemMainhand().getItem();

        if (player.getRidingEntity() instanceof RiftCreature) {
            RiftCreature creature = (RiftCreature) player.getRidingEntity();
            //detect left click
            if (!checkInItemWhitelist(heldItem) && event.getMouseButton() == 0) {
                if (event.getTicks() <= 10) {
                    RiftMessages.WRAPPER.sendToServer(new RiftMountControl(creature, 0));
                    KeyBinding.setKeyBindState(settings.keyBindAttack.getKeyCode(), false);
                }
            }
            //detect right click
            else if (!checkInItemWhitelist(heldItem) && !isFoodItem(heldItem) && event.getMouseButton() == 1) {
                if (event.isReleased()) {
                    RiftMessages.WRAPPER.sendToServer(new RiftMountControl(creature, 1, event.getTicks()));
                }
                KeyBinding.setKeyBindState(settings.keyBindUseItem.getKeyCode(), false);
            }
        }
    }

    //for stopping creatures from being able to be controlled in water when they got no energy
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void stopControlledMoveInWater(InputEvent.KeyInputEvent event) {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        EntityPlayer player = Minecraft.getMinecraft().player;

        if (player.getRidingEntity() instanceof RiftCreature) {
            RiftCreature creature = (RiftCreature) player.getRidingEntity();

            if (creature.isInWater() && creature.getEnergy() == 0) {
                if (settings.keyBindForward.isKeyDown()) {
                    KeyBinding.setKeyBindState(settings.keyBindForward.getKeyCode(), false);
                }
                else if (settings.keyBindBack.isKeyDown()) {
                    KeyBinding.setKeyBindState(settings.keyBindBack.getKeyCode(), false);
                }
                else if (settings.keyBindLeft.isKeyDown()) {
                    KeyBinding.setKeyBindState(settings.keyBindLeft.getKeyCode(), false);
                }
                else if (settings.keyBindRight.isKeyDown()) {
                    KeyBinding.setKeyBindState(settings.keyBindRight.getKeyCode(), false);
                }
            }
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
            Entity attacked = event.getEntity();
            if (!attacker.isTamed()) {
                if (attacked instanceof EntityTameable) {
                    if (!(((EntityTameable) attacked).isTamed())) {
                        event.setCanceled(true);
                    }
                }
                else if (!(attacked instanceof EntityPlayer)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    //make it so when mobs detect u as a target, they target the mounted creature instead
    @SubscribeEvent
    public void redirectToRiddenCreature(LivingSetAttackTargetEvent event) {
        if (event.getTarget() instanceof EntityPlayer) {
            if (event.getTarget().isRiding()) {
                if (event.getTarget().getRidingEntity() instanceof RiftCreature) {
                    ((EntityLiving)event.getEntityLiving()).setAttackTarget((RiftCreature)event.getTarget().getRidingEntity());
                }
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
            if (Item.getByNameOrId(itemEntry).equals(item)) return true;
        }
        return false;
    }

    private boolean isFoodItem(Item item) {
        return item instanceof ItemFood;
    }
}