package anightdazingzoroark.prift.server;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.entity.creature.*;
import anightdazingzoroark.prift.server.entity.RiftEntityProperties;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftCannon;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftCatapult;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftMortar;
import anightdazingzoroark.prift.server.items.RiftItems;
import anightdazingzoroark.prift.server.message.RiftManageCanUseControl;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftOnHitMultipart;
import com.google.common.base.Predicate;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import javax.annotation.Nullable;
import java.util.*;

public class ServerEvents {
    //make people join le discord
    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (GeneralConfig.showDiscordMessage) {
            TextComponentString message = new TextComponentString("Click here to join the Discord server for this mod to hang out and receive updates! We beg you!");
            message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/qVWaKRMCRc"));
            message.getStyle().setUnderlined(true);
            event.player.sendMessage(message);
        }
    }

    //make sure players cant move when trapped
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void noMoveWhileTrapped(InputEvent.KeyInputEvent event) {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        EntityPlayer player = Minecraft.getMinecraft().player;
        RiftEntityProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(player, RiftEntityProperties.class);

        if (properties.isCaptured) {
            if (settings.keyBindForward.isKeyDown()) {
                KeyBinding.setKeyBindState(settings.keyBindForward.getKeyCode(), false);
            }
            if (settings.keyBindBack.isKeyDown()) {
                KeyBinding.setKeyBindState(settings.keyBindBack.getKeyCode(), false);
            }
            if (settings.keyBindLeft.isKeyDown()) {
                KeyBinding.setKeyBindState(settings.keyBindLeft.getKeyCode(), false);
            }
            if (settings.keyBindRight.isKeyDown()) {
                KeyBinding.setKeyBindState(settings.keyBindRight.getKeyCode(), false);
            }
            if (settings.keyBindJump.isKeyDown()) {
                KeyBinding.setKeyBindState(settings.keyBindJump.getKeyCode(), false);
            }
        }
    }

    @SubscribeEvent
    public void noAttackWhileRiding(AttackEntityEvent event) {
        //prevent players from attackin while ridin
        if (event.getEntityPlayer().isRiding()) {
            if (event.getEntityPlayer().getRidingEntity() instanceof RiftCreature || event.getEntityPlayer().getRidingEntity() instanceof RiftLargeWeapon) {
                event.setCanceled(true);
            }
        }

        //ensure that damage towards hitboxes is same damage to creature
        if (event.getTarget() instanceof RiftCreaturePart && event.getEntity() instanceof EntityPlayer) {
            event.setCanceled(true);
            RiftCreaturePart part = (RiftCreaturePart) event.getTarget();
            RiftCreature parent = part.getParent();
            ((EntityPlayer) event.getEntity()).attackTargetEntityWithCurrentItem(parent);
            RiftMessages.WRAPPER.sendToServer(new RiftOnHitMultipart(parent));
        }
    }

    //prevent players from breaking blocks while ridin
    @SubscribeEvent
    public void noBlockBreakWhileRiding(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntityPlayer().isRiding()) {
            if (event.getEntityPlayer().getRidingEntity() instanceof RiftCreature || event.getEntityPlayer().getRidingEntity() instanceof RiftLargeWeapon) {
                event.setUseBlock(null);
                event.setCanceled(true);
            }
        }
    }

    //prevent players from using interactions when ridin
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getEntityPlayer().isRiding()) {
            if (event.getEntityPlayer().getRidingEntity() instanceof RiftCreature || event.getEntityPlayer().getRidingEntity() instanceof RiftLargeWeapon) {
                event.setCanceled(true);
            }
        }
    }

    //prevent players from placin blocks while ridin
    @SubscribeEvent
    public void noBlockPlaceWhileRiding(BlockEvent.PlaceEvent event) {
        if (event.getPlayer().isRiding()) {
            if (event.getPlayer().getRidingEntity() instanceof RiftCreature || event.getPlayer().getRidingEntity() instanceof RiftLargeWeapon) {
                event.setCanceled(true);
            }
        }
    }

    //ensure parasaurs always do 0 damage
    @SubscribeEvent
    public void zeroDamage(LivingDamageEvent event) {
        if (event.getSource().getTrueSource() instanceof Parasaurolophus) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void gainXpFromKill(LivingDeathEvent event) {
        //give xp to creature for every creature it kills
        if (event.getSource().getTrueSource() instanceof RiftCreature) {
            RiftCreature creature = (RiftCreature) event.getSource().getTrueSource();
            if (creature.isTamed()) {
                if (event.getEntity() instanceof EntityLiving) {
                    EntityLiving entityLiving = (EntityLiving) event.getEntity();
                    int newXp = 5 * entityLiving.getExperiencePoints((EntityPlayer) creature.getOwner());
                    creature.setXP(creature.getXP() + newXp);
                }
            }
        }

        //make arthropods drop chitin and hemolymph
        if (!event.getEntityLiving().world.isRemote) {
            if (event.getEntityLiving().getCreatureAttribute().equals(EnumCreatureAttribute.ARTHROPOD) && !(event.getEntityLiving() instanceof RiftCreature)) {
                event.getEntityLiving().entityDropItem(new ItemStack(RiftItems.CHITIN, RiftUtil.randomInRange(1, 3)), 0);
                if (event.getEntityLiving().isBurning()) event.getEntityLiving().entityDropItem(new ItemStack(RiftItems.COOKED_HEMOLYMPH, RiftUtil.randomInRange(1, 3)), 0);
                else event.getEntityLiving().entityDropItem(new ItemStack(RiftItems.RAW_HEMOLYMPH, RiftUtil.randomInRange(1, 3)), 0);
            }
        }
    }

    //manage setting creature workstation
    @SubscribeEvent
    public void setCreatureWorkstation(PlayerInteractEvent.RightClickBlock event) {
        RiftEntityProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(event.getEntityPlayer(), RiftEntityProperties.class);
        if (properties.settingCreatureWorkstation) {
            RiftCreature creature = (RiftCreature) event.getWorld().getEntityByID(properties.creatureIdForWorkstation);
            IWorkstationUser workstationUser = (IWorkstationUser) creature;
            IBlockState iblockstate = event.getWorld().getBlockState(event.getPos());
            if (iblockstate.getMaterial() != Material.AIR) {
                boolean canUseFlag = true;
                List<RiftCreature> workstationUsers = event.getWorld().getEntities(RiftCreature.class, new Predicate<RiftCreature>() {
                    @Override
                    public boolean apply(@Nullable RiftCreature input) {
                        return input instanceof IWorkstationUser;
                    }
                });
                for (RiftCreature creature1 : workstationUsers) {
                    if (creature1.getWorkstationPos().equals(event.getPos())) {
                        canUseFlag = false;
                        break;
                    }
                }

                if (canUseFlag) {
                    if (workstationUser.isWorkstation(event.getPos())) {
                        event.setCanceled(true);
                        creature.setUseWorkstation(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
                        event.getEntityPlayer().sendStatusMessage(new TextComponentTranslation("action.set_creature_workstation_success"), false);
                    }
                    else event.getEntityPlayer().sendStatusMessage(new TextComponentTranslation("action.set_creature_workstation_fail"), false);
                }
                else event.getEntityPlayer().sendStatusMessage(new TextComponentTranslation("action.creature_workstation_already_used"), false);

                properties.settingCreatureWorkstation = false;
                properties.creatureIdForWorkstation = -1;
            }
        }
    }

    //manage adding new drops to blocks
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.HarvestDropsEvent event) {
        Block block = event.getState().getBlock();
        IBlockState blockState = event.getState();
        if (GeneralConfig.truffleSpawning) {
            List<String> blocksArray = Arrays.asList(GeneralConfig.truffleBlocks);
            for (String blockEntry : blocksArray) {
                int itemColonPos = blockEntry.indexOf(":", blockEntry.indexOf(":") + 1);
                int blockData = Integer.parseInt(blockEntry.substring(itemColonPos + 1));
                String blockName = blockEntry.substring(0, itemColonPos);
                if (Block.getBlockFromName(blockName).equals(block) && (blockData == - 1 || block.getMetaFromState(blockState) == blockData)) {
                    Biome blockBiome = event.getWorld().getBiome(event.getPos());

                    List<String> wlistBiomes = new ArrayList<>();
                    List<String> blistBiomes = new ArrayList<>();
                    List<String> wlistTags = new ArrayList<>();
                    List<String> blistTags = new ArrayList<>();

                    for (String biomeEntry : GeneralConfig.truffleBiomes) {
                        int firstPos = biomeEntry.indexOf(":");
                        String newName = biomeEntry.substring(firstPos + 1);

                        if (biomeEntry.substring(0, 1).equals("-")) {
                            if (biomeEntry.substring(1, firstPos).equals("tag")) blistTags.add(newName);
                            else if (biomeEntry.substring(1, firstPos).equals("biome")) blistBiomes.add(newName);
                        }
                        else {
                            if (biomeEntry.substring(0, firstPos).equals("tag")) wlistTags.add(newName);
                            else if (biomeEntry.substring(0, firstPos).equals("biome")) wlistBiomes.add(newName);
                        }
                    }

                    if ((wlistBiomes.contains(blockBiome.getRegistryName().toString())
                            || RiftUtil.biomeTagMatchFromList(wlistTags, blockBiome))
                            && !blistBiomes.contains(blockBiome.getRegistryName().toString())
                            && !RiftUtil.biomeTagMatchFromList(blistTags, blockBiome)
                    ) {
                        if (new Random().nextDouble() <= GeneralConfig.truffleChance) {
                            int lowVal = Integer.parseInt(GeneralConfig.truffleAmntRange[0]);
                            int hiVal = Integer.parseInt(GeneralConfig.truffleAmntRange[1]);
                            event.getDrops().add(new ItemStack(RiftItems.TRUFFLE, RiftUtil.randomInRange(lowVal, hiVal)));
                        }
                        break;
                    }
                }
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

    //i forgor what tf this does :skull:
    //probably best if it doesnt get deleted
    @SubscribeEvent
    public void onStartRiding(EntityMountEvent event) {
        if (event.isDismounting() && event.getEntityMounting() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)event.getEntityMounting();
            RiftEntityProperties playerProperties = EntityPropertiesHandler.INSTANCE.getProperties(player, RiftEntityProperties.class);
            if (event.getEntityBeingMounted() instanceof RiftCreature) {
                RiftCreature creature = (RiftCreature) event.getEntityBeingMounted();
                RiftEntityProperties creatureProperties = EntityPropertiesHandler.INSTANCE.getProperties(creature, RiftEntityProperties.class);

                RiftMessages.WRAPPER.sendToServer(new RiftManageCanUseControl(creature, 1, false));
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
        if (!GeneralConfig.canDropFromCreatureKill) {
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
        //make it so that when a mob tries to target an invisible anomalocaris nothing happens
        if (event.getTarget() instanceof Anomalocaris) {
            Anomalocaris anomalocaris = (Anomalocaris)event.getTarget();
            if (anomalocaris.isUsingInvisibility()) ((EntityLiving)event.getEntityLiving()).setAttackTarget(null);
        }
        //make it so when a player uses a large weapon on a mob, the mob will go after its operator
        if (event.getTarget() instanceof RiftLargeWeapon) {
            RiftLargeWeapon weapon = (RiftLargeWeapon) event.getTarget();
            ((EntityLiving)event.getEntityLiving()).setAttackTarget((EntityLivingBase) weapon.getPassengers().get(0));
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

            RiftCreature creature = null;
            if (entity instanceof RiftCreature) creature = (RiftCreature) entity;
            if (creature != null) {
                //make sure that if a creature's target dies, their path is cleared
                if (creature.getAttackTarget() != null) {
                    if (!creature.getAttackTarget().isEntityAlive()) creature.getNavigator().clearPath();
                }
            }
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

        //make sure mobs dont fall when trapped
        if (properties.isCaptured) entity.fallDistance = 0;
    }

    //reset some properties (like bleeding) upon death
    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        RiftEntityProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(entity, RiftEntityProperties.class);
        properties.resetBleeding();
        properties.isCaptured = false;
    }

    //manage cannon impacting stuff
    @SubscribeEvent
    public void manageProjectileExplosion(ExplosionEvent.Detonate event) {
        //manage cannon explosion stuff
        if (event.getExplosion().getExplosivePlacedBy() instanceof RiftCannon) {
            RiftCannon cannon = (RiftCannon) event.getExplosion().getExplosivePlacedBy();
            EntityPlayer user = (EntityPlayer) cannon.getPassengers().get(0);
            //remove cannon and user
            event.getAffectedEntities().remove(cannon);
            event.getAffectedEntities().remove(user);
            //remove creatures tamed to user
            List<EntityTameable> tamedEntities = new ArrayList<>();
            for (Entity entity : event.getAffectedEntities()) {
                if (entity instanceof EntityTameable) {
                    if ((((EntityTameable) entity).isTamed())) {
                        if (!((EntityTameable) entity).getOwner().equals(user)) {
                            tamedEntities.add((EntityTameable) entity);
                        }
                    }
                }
            }
            event.getAffectedEntities().removeAll(tamedEntities);
        }
        //manage catapult explosion stuff
        if (event.getExplosion().getExplosivePlacedBy() instanceof RiftCatapult) {
            RiftCatapult catapult = (RiftCatapult) event.getExplosion().getExplosivePlacedBy();
            EntityPlayer user = (EntityPlayer) catapult.getPassengers().get(0);
            //remove catapult and user
            event.getAffectedEntities().remove(catapult);
            event.getAffectedEntities().remove(user);
            //remove creatures tamed to user
            List<EntityTameable> tamedEntities = new ArrayList<>();
            for (Entity entity : event.getAffectedEntities()) {
                if (entity instanceof EntityTameable) {
                    if ((((EntityTameable) entity).isTamed())) {
                        if (!((EntityTameable) entity).getOwner().equals(user)) {
                            tamedEntities.add((EntityTameable) entity);
                        }
                    }
                }
            }
            event.getAffectedEntities().removeAll(tamedEntities);
            //make all affected entities get slowness 255 for 5 seconds when hit
            for (Entity entity : event.getAffectedEntities()) {
                if (entity instanceof EntityLivingBase) {
                    ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 255));
                }
            }
        }
        //manage mortar explosion stuff
        if (event.getExplosion().getExplosivePlacedBy() instanceof RiftMortar) {
            RiftMortar mortar = (RiftMortar) event.getExplosion().getExplosivePlacedBy();
            EntityPlayer user = (EntityPlayer) mortar.getPassengers().get(0);
            //remove catapult and user
            event.getAffectedEntities().remove(mortar);
            event.getAffectedEntities().remove(user);
            //remove creatures tamed to user
            List<EntityTameable> tamedEntities = new ArrayList<>();
            for (Entity entity : event.getAffectedEntities()) {
                if (entity instanceof EntityTameable) {
                    if ((((EntityTameable) entity).isTamed())) {
                        if (!((EntityTameable) entity).getOwner().equals(user)) {
                            tamedEntities.add((EntityTameable) entity);
                        }
                    }
                }
            }
            event.getAffectedEntities().removeAll(tamedEntities);
        }
        //manage explosions from apato weapons
        if (event.getExplosion().getExplosivePlacedBy() instanceof Apatosaurus) {
            Apatosaurus apatosaurus = (Apatosaurus) event.getExplosion().getExplosivePlacedBy();
            EntityPlayer user = (EntityPlayer) apatosaurus.getControllingPassenger();
            //remove catapult and user
            event.getAffectedEntities().remove(apatosaurus);
            event.getAffectedEntities().remove(user);
            //remove creatures tamed to user
            List<EntityTameable> tamedEntities = new ArrayList<>();
            for (Entity entity : event.getAffectedEntities()) {
                if (entity instanceof EntityTameable) {
                    if ((((EntityTameable) entity).isTamed())) {
                        if (!((EntityTameable) entity).getOwner().equals(user)) {
                            tamedEntities.add((EntityTameable) entity);
                        }
                    }
                }
            }
            event.getAffectedEntities().removeAll(tamedEntities);
        }
    }
}