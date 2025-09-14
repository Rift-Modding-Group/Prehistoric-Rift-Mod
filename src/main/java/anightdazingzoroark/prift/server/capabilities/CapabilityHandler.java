package anightdazingzoroark.prift.server.capabilities;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.RiftDamage;
import anightdazingzoroark.prift.server.capabilities.creatureBoxData.CreatureBoxDataProvider;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.INonPotionEffects;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsProvider;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressProvider;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapabilityHandler {
    public static final ResourceLocation PLAYER_TAMED_CREATURES_CAPABILITY = new ResourceLocation(RiftInitialize.MODID, "playertamedcreatures");
    public static final ResourceLocation PLAYER_JOURNAL_PROGRESS_CAPABILITY = new ResourceLocation(RiftInitialize.MODID, "playerjournalprogress");
    public static final ResourceLocation NON_POTION_EFFECTS = new ResourceLocation(RiftInitialize.MODID, "nonpotioneffects");
    public static final ResourceLocation CREATURE_BOX_DATA = new ResourceLocation(RiftInitialize.MODID, "creatureboxdata");

    @SubscribeEvent
    public void attachCapabilityToEntity(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(PLAYER_TAMED_CREATURES_CAPABILITY, new PlayerTamedCreaturesProvider());
            event.addCapability(PLAYER_JOURNAL_PROGRESS_CAPABILITY, new PlayerJournalProgressProvider());
        }
        else if (event.getObject() instanceof EntityLivingBase) {
            event.addCapability(NON_POTION_EFFECTS, new NonPotionEffectsProvider());
        }
    }

    @SubscribeEvent
    public void attachCapabilityToWorld(AttachCapabilitiesEvent<World> event) {
        event.addCapability(CREATURE_BOX_DATA, new CreatureBoxDataProvider());
    }

    @SubscribeEvent
    public void onEntityJoinWorldPlayerTamedCreatures(EntityJoinWorldEvent event) {
        if (!event.getEntity().world.isRemote && event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            IPlayerTamedCreatures tamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
            if (tamedCreatures == null) return;
            RiftMessages.WRAPPER.sendToAll(new RiftUpdatePlayerTamedCreatures(PlayerTamedCreaturesProvider.writeNBT(tamedCreatures, null), player));
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorldPlayerJournalProgress(EntityJoinWorldEvent event) {
        if (!event.getEntity().world.isRemote && event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            IPlayerJournalProgress journalProgress = player.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
            if (journalProgress == null) return;
            RiftMessages.WRAPPER.sendToAll(new RiftUpdatePlayerJournalProgress(PlayerJournalProgressProvider.writeNBT(journalProgress, null), player));
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorldNonPotionEffects(EntityJoinWorldEvent event) {
        if (!event.getEntity().world.isRemote && event.getEntity() instanceof EntityLivingBase) {
            INonPotionEffects nonPotionEffects = event.getEntity().getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
            if (nonPotionEffects == null) return;
            RiftMessages.WRAPPER.sendToAll(new RiftUpdateNonPotionEffects(NonPotionEffectsProvider.writeNBT(nonPotionEffects, null), (EntityLivingBase) event.getEntity()));
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        EntityPlayer player = event.getEntityPlayer();

        //replicate tamed creatures
        IPlayerTamedCreatures tamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        IPlayerTamedCreatures oldTamedCreatures = event.getOriginal().getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        tamedCreatures.setPartyNBT(oldTamedCreatures.getPartyNBT());
        tamedCreatures.setBoxNBT(oldTamedCreatures.getBoxNBT());
        tamedCreatures.setLastSelected(oldTamedCreatures.getLastSelected());
        tamedCreatures.setPartyLastOpenedTime(oldTamedCreatures.getPartyLastOpenedTime());
        tamedCreatures.setBoxLastOpenedTime(oldTamedCreatures.getBoxLastOpenedTime());

        //replicate journal progress
        IPlayerJournalProgress journalProgress = player.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
        IPlayerJournalProgress oldJournalProgress = event.getOriginal().getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);

        journalProgress.setEncounteredCreatures(oldJournalProgress.getEncounteredCreatures());

        //replicate effects
        INonPotionEffects nonPotionEffects = player.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        INonPotionEffects oldNonPotionEffects = player.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        if (!event.isWasDeath()) {
            if (oldNonPotionEffects.isBleeding()) nonPotionEffects.setBleeding(oldNonPotionEffects.getBleedStrength(), oldNonPotionEffects.getBleedTick());

            nonPotionEffects.setGrabbed(oldNonPotionEffects.isGrabbed());

            if (oldNonPotionEffects.isBolaCaptured()) nonPotionEffects.setBolaCaptured(oldNonPotionEffects.getBolaCapturedTick());
        }
    }

    //manage effects
    @SubscribeEvent
    public void forEachEntityTick(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();

        //client side
        if (entity.world.isRemote) {
            INonPotionEffects nonPotionEffects = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
            if (nonPotionEffects == null) return;

            //manage bleed particles
            if (nonPotionEffects.isBleeding()) {
                double motionY = RiftUtil.randomInRange(1D, 1.5D);
                double f = entity.getRNG().nextFloat() * (entity.getEntityBoundingBox().maxX - entity.getEntityBoundingBox().minX) + entity.getEntityBoundingBox().minX;
                double f1 = entity.getRNG().nextFloat() * (entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) + entity.getEntityBoundingBox().minY;
                double f2 = entity.getRNG().nextFloat() * (entity.getEntityBoundingBox().maxZ - entity.getEntityBoundingBox().minZ) + entity.getEntityBoundingBox().minZ;
                RiftInitialize.PROXY.spawnParticle("bleed", f, f1, f2, 0D, motionY, 0D);
            }

            //manage bola capture
            //this is where force falling upon being captured happens
            if (nonPotionEffects.isBolaCaptured()) {
                entity.motionY -= 0.5;
                entity.velocityChanged = true;
            }
        }
        //server side
        else {
            INonPotionEffects nonPotionEffects = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
            if (nonPotionEffects == null) return;

            //manage bleed
            //this is where damage and tick down take place
            if (nonPotionEffects.isBleeding()) {
                nonPotionEffects.reduceBleedTick();
                //stop bleed on client side
                if (nonPotionEffects.getBleedTick() == 0) RiftMessages.WRAPPER.sendToAll(new RiftStopBleeding(entity));
                if ((nonPotionEffects.getBleedTick() / 20) % 2 == 0) entity.attackEntityFrom(RiftDamage.RIFT_BLEED, nonPotionEffects.getBleedStrength() + 1F);
            }

            //manage bola capture
            //this is where applying permanent slowness and tick down take place
            if (nonPotionEffects.isBolaCaptured()) {
                nonPotionEffects.reduceBolaCapturedTick();
                if (nonPotionEffects.getBolaCapturedTick() == 0) RiftMessages.WRAPPER.sendToAll(new RiftResetBolaCaptured(entity));
                entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 255));
            }

            //manage hypnosis effect
            if (nonPotionEffects.isHypnotized()) {
                //if entity is not EntityCreature, ignore everything
                if (!(entity instanceof EntityCreature)) return;

                EntityCreature entityCreature = (EntityCreature) entity;
                RiftCreature hypnotizer = nonPotionEffects.getHypnotizer(entity.world);

                //if the hypnotizer no longer exists in the world, go commit die
                if (hypnotizer == null || !hypnotizer.isEntityAlive()) {
                    entityCreature.onKillCommand();
                    return;
                }

                //hypnosis logic management
                //always make sure hypnotized mob is within 4 block radius of hypnotizer
                //if not, move to the hypnotizer
                //also, clear any paths if it generates one and is within range of hypnotizer
                if (entityCreature.getDistance(hypnotizer) > 4f && entityCreature.getNavigator().noPath()) {
                    entityCreature.getNavigator().tryMoveToXYZ(hypnotizer.posX, hypnotizer.posY, hypnotizer.posZ, 1D);
                }
                else if (entityCreature.getDistance(hypnotizer) <= 4f && !entityCreature.getNavigator().noPath()) {
                    entityCreature.getNavigator().clearPath();
                }

                boolean hypnotizerHasTarget = hypnotizer.getAttackTarget() != null && hypnotizer.getAttackTarget().isEntityAlive();
                boolean hypnotizedHasTarget = entityCreature.getAttackTarget() != null && entityCreature.getAttackTarget().isEntityAlive();
                boolean hypnotizedTargeting = (hypnotizerHasTarget && hypnotizedHasTarget) && hypnotizer.getAttackTarget().equals(entityCreature.getAttackTarget());

                //if hypnotizer has a target, this mob targets it too
                if (hypnotizerHasTarget && !hypnotizedHasTarget) {
                    entityCreature.setAttackTarget(hypnotizer.getAttackTarget());
                }
                //if hypnotizer and this mob have a target but are different
                //set the mobs target to the hypnotizers target
                else if (hypnotizerHasTarget && hypnotizedHasTarget && !hypnotizedTargeting) {
                    entityCreature.setAttackTarget(hypnotizer.getAttackTarget());
                }
                //if hypnotizer has no target and this mob finds one
                //clear this mobs target and pathing
                else if (!hypnotizerHasTarget && hypnotizedHasTarget) {
                    entityCreature.setAttackTarget(null);
                }
            }
        }
    }
}
