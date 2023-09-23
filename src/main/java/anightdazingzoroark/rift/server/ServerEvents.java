package anightdazingzoroark.rift.server;

import anightdazingzoroark.rift.RiftUtil;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import anightdazingzoroark.rift.server.events.RiftMouseHoldEvent;
import anightdazingzoroark.rift.server.message.RiftManageCanUseRightClick;
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
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import anightdazingzoroark.rift.compat.shouldersurfingreloaded.SSRCompat;

public class ServerEvents {
    private int rightClickFill = 0;
    private boolean rCTrigger = true;
    private boolean canTakeFallDamage = true;

    //for controlling when u use attacks or abilities while riding creatures
    @SubscribeEvent(receiveCanceled = true)
    public void mouseUse(RiftMouseHoldEvent event) {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        EntityPlayer player = Minecraft.getMinecraft().player;
        Item heldItem = player.getHeldItemMainhand().getItem();

        if (player.getRidingEntity() instanceof RiftCreature && !Loader.isModLoaded(SSRCompat.SSR_MOD_ID)) {
            RiftCreature creature = (RiftCreature) player.getRidingEntity();
            //detect left click
            if (!RiftUtil.checkInMountItemWhitelist(heldItem) && event.getMouseButton() == 0) {
                if (event.getTicks() <= 10) {
                    RiftMessages.WRAPPER.sendToServer(new RiftMountControl(creature, 0));
                    KeyBinding.setKeyBindState(settings.keyBindAttack.getKeyCode(), false);
                }
            }
            //detect right click
            //also has system that ensures that tamed creatures dont use right click related stuff the moment they're mounted
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

    @SubscribeEvent
    public void onStartRiding(EntityMountEvent event) {
        if (event.isDismounting() && event.getEntityMounting() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)event.getEntityMounting();
            if (event.getEntityBeingMounted() instanceof RiftCreature) {
                RiftCreature creature = (RiftCreature) event.getEntityBeingMounted();
                RiftMessages.WRAPPER.sendToServer(new RiftManageCanUseRightClick(creature, false));
                canTakeFallDamage = false;
            }
        }
    }

    @SubscribeEvent
    public void reduceDamage(LivingHurtEvent event) {
        //when a tamed creature gets attacked by a wild creature of the same type, the damage they received is halved
        if (event.getEntity() instanceof RiftCreature && event.getSource().getTrueSource() instanceof RiftCreature) {
            RiftCreature creature = (RiftCreature) event.getEntity();
            RiftCreature attacker = (RiftCreature) event.getSource().getTrueSource();
            if (creature.isTamed() && !attacker.isTamed() && creature.creatureType == attacker.creatureType) {
                event.setAmount(event.getAmount() / 2);
            }
        }

        //players shouldn't take any fall damage after dismounting
        if (event.getSource() == DamageSource.FALL && !canTakeFallDamage) {
            event.setCanceled(true);
            canTakeFallDamage = true;
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
}