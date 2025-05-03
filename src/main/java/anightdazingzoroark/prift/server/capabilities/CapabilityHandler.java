package anightdazingzoroark.prift.server.capabilities;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.INonPotionEffects;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsProvider;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressProvider;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.message.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapabilityHandler {
    public static final ResourceLocation PLAYER_TAMED_CREATURES_CAPABILITY = new ResourceLocation(RiftInitialize.MODID, "playertamedcreatures");
    public static final ResourceLocation PLAYER_JOURNAL_PROGRESS_CAPABILITY = new ResourceLocation(RiftInitialize.MODID, "playerjournalprogress");
    public static final ResourceLocation NON_POTION_EFFECTS = new ResourceLocation(RiftInitialize.MODID, "nonpotioneffects");

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(PLAYER_TAMED_CREATURES_CAPABILITY, new PlayerTamedCreaturesProvider());
            event.addCapability(PLAYER_JOURNAL_PROGRESS_CAPABILITY, new PlayerJournalProgressProvider());
        }
        else if (event.getObject() instanceof EntityLivingBase) {
            event.addCapability(NON_POTION_EFFECTS, new NonPotionEffectsProvider());
        }
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
        tamedCreatures.setPartySizeLevel(oldTamedCreatures.getPartySizeLevel());
        tamedCreatures.setBoxSizeLevel(oldTamedCreatures.getBoxSizeLevel());
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
        if (entity.world.isRemote) {
            INonPotionEffects nonPotionEffects = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
            if (nonPotionEffects == null) return;

            //manage bleeding
            double fallMotion = !entity.onGround ? entity.motionY : 0;
            boolean isMoving =  Math.sqrt((entity.motionX * entity.motionX) + (fallMotion * fallMotion) + (entity.motionZ * entity.motionZ)) > 0;
            if (nonPotionEffects.isBleeding()) {
                nonPotionEffects.reduceBleedTick();
                RiftMessages.WRAPPER.sendToServer(new RiftManageBleeding(entity, isMoving));

                double motionY = RiftUtil.randomInRange(1D, 1.5D);
                double f = entity.getRNG().nextFloat() * (entity.getEntityBoundingBox().maxX - entity.getEntityBoundingBox().minX) + entity.getEntityBoundingBox().minX;
                double f1 = entity.getRNG().nextFloat() * (entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) + entity.getEntityBoundingBox().minY;
                double f2 = entity.getRNG().nextFloat() * (entity.getEntityBoundingBox().maxZ - entity.getEntityBoundingBox().minZ) + entity.getEntityBoundingBox().minZ;
                RiftInitialize.PROXY.spawnParticle("bleed", f, f1, f2, 0D, motionY, 0D);
            }

            //manage bola capture
            if (nonPotionEffects.isBolaCaptured()) {
                nonPotionEffects.reduceBolaCapturedTick();
                RiftMessages.WRAPPER.sendToServer(new RiftManageBolaCaptured(entity));
                entity.motionY -= 0.5;
                entity.velocityChanged = true;
            }
        }
    }
}
