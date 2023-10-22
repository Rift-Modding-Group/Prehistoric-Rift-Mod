package anightdazingzoroark.rift.server;

import anightdazingzoroark.rift.RiftUtil;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import anightdazingzoroark.rift.server.entity.RiftEntityProperties;
import anightdazingzoroark.rift.server.events.RiftMouseHoldEvent;
import anightdazingzoroark.rift.server.message.RiftIncrementClickUse;
import anightdazingzoroark.rift.server.message.RiftManageCanUseClick;
import anightdazingzoroark.rift.server.message.RiftMessages;
import anightdazingzoroark.rift.server.message.RiftMountControl;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import anightdazingzoroark.rift.compat.shouldersurfingreloaded.SSRCompat;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ServerEvents {
    //for controlling when u use attacks or abilities while riding creatures
    @SubscribeEvent(receiveCanceled = true)
    public void mouseUse(RiftMouseHoldEvent event) {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        EntityPlayer player = Minecraft.getMinecraft().player;
        Item heldItem = player.getHeldItemMainhand().getItem();
        RiftEntityProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(player, RiftEntityProperties.class);

        if (player.getRidingEntity() instanceof RiftCreature && !Loader.isModLoaded(SSRCompat.SSR_MOD_ID)) {
            RiftCreature creature = (RiftCreature) player.getRidingEntity();
            //detect left click
            if (!RiftUtil.checkInMountItemWhitelist(heldItem) && event.getMouseButton() == 0) {
                if (creature.hasLeftClickChargeBar()) {
                    if (!event.isReleased()) {
                        properties.leftClickFill++;
                        RiftMessages.WRAPPER.sendToServer(new RiftIncrementClickUse(creature, 0));
                    }
                    else {
                        RiftMessages.WRAPPER.sendToServer(new RiftMountControl(creature, 0, properties.leftClickFill));
                        properties.leftClickFill = 0;
                    }
                }
                else {
                    if (event.getTicks() <= 10) RiftMessages.WRAPPER.sendToServer(new RiftMountControl(creature, 0));
                }
            }
            //detect right click
            //also has system that ensures that tamed creatures dont use right click related stuff the moment they're mounted
            else if (!RiftUtil.checkInMountItemWhitelist(heldItem) && !(heldItem instanceof ItemFood) && event.getMouseButton() == 1) {
                //dont trigger immediately after riding
                if (!properties.rCTrigger && event.isReleased()) properties.rCTrigger = true;

                if (properties.rCTrigger) {
                    if (creature.getRightClickCooldown() == 0) {
                        if (!event.isReleased()) {
                            properties.rightClickFill++;
                            RiftMessages.WRAPPER.sendToServer(new RiftIncrementClickUse(creature, 1));
                        }
                        else if (event.isReleased() && !creature.canUseRightClick()) {
                            RiftMessages.WRAPPER.sendToServer(new RiftManageCanUseClick(creature, 1, true));
                            properties.rightClickFill = 0;
                        }
                        else if (event.isReleased()) {
                            RiftMessages.WRAPPER.sendToServer(new RiftMountControl(creature, 1, properties.rightClickFill));
                            properties.rightClickFill = 0;
                        }
                    }
                }
            }
        }
    }

    //for stopping default mouse actions when riding
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void stopDefaultMouseActions(InputEvent.MouseInputEvent event) {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        EntityPlayer player = Minecraft.getMinecraft().player;
        Item heldItem = player.getHeldItemMainhand().getItem();

        if (player.getRidingEntity() instanceof RiftCreature) {
            if (!RiftUtil.checkInMountItemWhitelist(heldItem) && settings.keyBindAttack.isPressed()) {
                KeyBinding.setKeyBindState(settings.keyBindAttack.getKeyCode(), false);
            }
            else if (!RiftUtil.checkInMountItemWhitelist(heldItem) && !(heldItem instanceof ItemFood) && settings.keyBindUseItem.isPressed()) {
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
            RiftEntityProperties playerProperties = EntityPropertiesHandler.INSTANCE.getProperties(player, RiftEntityProperties.class);
            if (event.getEntityBeingMounted() instanceof RiftCreature) {
                RiftCreature creature = (RiftCreature) event.getEntityBeingMounted();
                RiftEntityProperties creatureProperties = EntityPropertiesHandler.INSTANCE.getProperties(creature, RiftEntityProperties.class);

                RiftMessages.WRAPPER.sendToServer(new RiftManageCanUseClick(creature, 1, false));
                if (playerProperties != null) playerProperties.ridingCreature = true;
                creatureProperties.rCTrigger = false;
            }
        }
    }

    @SubscribeEvent
    public void attackEvent(LivingHurtEvent event) {
        //when a tamed creature gets attacked by a wild creature of the same type, the damage they received is halved
        if (event.getEntity() instanceof RiftCreature && event.getSource().getTrueSource() instanceof RiftCreature) {
            RiftCreature creature = (RiftCreature) event.getEntity();
            RiftCreature attacker = (RiftCreature) event.getSource().getTrueSource();
            if (creature.isTamed() && !attacker.isTamed() && creature.creatureType == attacker.creatureType) {
                event.setAmount(event.getAmount() / 2);
            }
        }
    }

    //prevent player from takin damage when dismountin tamed creature
    @SubscribeEvent
    public void noDamageWhenDismounting(LivingFallEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)event.getEntityLiving();
            RiftEntityProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(player, RiftEntityProperties.class);

            if (properties.ridingCreature) {
                event.setDamageMultiplier(0);
                properties.ridingCreature = false;
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

    @SubscribeEvent
    public void onSetTarget(LivingSetAttackTargetEvent event) {
        //make it so when mobs detect u as a target, they target the mounted creature instead
        if (event.getTarget() instanceof EntityPlayer) {
            if (event.getTarget().isRiding()) {
                if (event.getTarget().getRidingEntity() instanceof RiftCreature) {
                    ((EntityLiving)event.getEntityLiving()).setAttackTarget((RiftCreature)event.getTarget().getRidingEntity());
                }
            }
        }
    }

    @SubscribeEvent
    public void forEachEntityTick(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        RiftEntityProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(entity, RiftEntityProperties.class);
        if (!entity.world.isRemote) {
            double fallMotion = !entity.onGround ? entity.motionY : 0;
            boolean isMoving =  Math.sqrt((entity.motionX * entity.motionX) + (fallMotion * fallMotion) + (entity.motionZ * entity.motionZ)) > 0;
            //manage bleeding
            if (properties.isBleeding) {
                if (isMoving) entity.attackEntityFrom(DamageSource.GENERIC, (float)properties.bleedingStrength + 1F);
                else entity.attackEntityFrom(DamageSource.GENERIC, (float)(properties.bleedingStrength + 1) * 2F);

                properties.ticksUntilStopBleeding--;
            }
            if (properties.ticksUntilStopBleeding <= 0) properties.resetBleeding();
        }
    }
}