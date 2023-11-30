package anightdazingzoroark.prift.server;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.RiftEntityProperties;
import anightdazingzoroark.prift.server.message.RiftManageCanUseClick;
import anightdazingzoroark.prift.server.message.RiftMessages;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class ServerEvents {
    //prevent players from attackin while ridin
    @SubscribeEvent
    public void noAttackWhileRiding(AttackEntityEvent event) {
        if (event.getEntityPlayer().isRiding()) {
            if (event.getEntityPlayer().getRidingEntity() instanceof RiftCreature) {
                event.setCanceled(true);
            }
        }
    }

    //prevent players from breaking blocks while ridin
    @SubscribeEvent
    public void noBlockBreakWhileRiding(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntityPlayer().isRiding()) {
            if (event.getEntityPlayer().getRidingEntity() instanceof RiftCreature) {
                event.setUseBlock(null);
                event.setCanceled(true);
            }
        }
    }

    //prevent players from placin blocks while ridin
    @SubscribeEvent
    public void noBlockPlaceWhileRiding(BlockEvent.PlaceEvent event) {
        if (event.getPlayer().isRiding()) {
            if (event.getPlayer().getRidingEntity() instanceof RiftCreature) {
                event.setCanceled(true);
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
        //tamed creatures cannot hurt their owners
        if (event.getEntity() instanceof EntityPlayer && event.getSource().getTrueSource() instanceof RiftCreature) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            RiftCreature attacker = (RiftCreature) event.getSource().getTrueSource();
            if (attacker.isTamed()) {
                if (attacker.getOwner() == player) event.setCanceled(true);
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
                if (isMoving) entity.attackEntityFrom(RiftDamage.RIFT_BLEED, (float)properties.bleedingStrength + 1F);
                else entity.attackEntityFrom(RiftDamage.RIFT_BLEED, (float)(properties.bleedingStrength + 1) * 2F);

                double motionY = RiftUtil.randomInRange(-0.75D, -0.25D);
                double f = entity.getRNG().nextFloat() * (entity.getEntityBoundingBox().maxX - entity.getEntityBoundingBox().minX) + entity.getEntityBoundingBox().minX;
                double f1 = entity.getRNG().nextFloat() * (entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) + entity.getEntityBoundingBox().minY;
                double f2 = entity.getRNG().nextFloat() * (entity.getEntityBoundingBox().maxZ - entity.getEntityBoundingBox().minZ) + entity.getEntityBoundingBox().minZ;

                RiftInitialize.PROXY.spawnParticle("bleed", f, f1, f2, 0D, motionY, 0D);

                properties.ticksUntilStopBleeding--;
            }
            if (properties.ticksUntilStopBleeding <= 0) properties.resetBleeding();
        }
        else {
            //manage swing disable when riding creature
            if (entity instanceof EntityPlayer && entity.isRiding()) {
                if (entity.getRidingEntity() instanceof RiftCreature) {
                    if (entity.isSwingInProgress) {
                        entity.swingProgressInt = 0;
                        entity.isSwingInProgress = false;
                        entity.swingingHand = null;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        RiftEntityProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(entity, RiftEntityProperties.class);
        //for remove bleed
        properties.resetBleeding();
    }
}