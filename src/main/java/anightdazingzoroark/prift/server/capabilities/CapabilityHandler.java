package anightdazingzoroark.prift.server.capabilities;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressProvider;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftUpdatePlayerJournalProgress;
import anightdazingzoroark.prift.server.message.RiftUpdatePlayerTamedCreatures;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapabilityHandler {
    public static final ResourceLocation PLAYER_TAMED_CREATURES_CAPABILITY = new ResourceLocation(RiftInitialize.MODID, "playertamedcreatures");
    public static final ResourceLocation PLAYER_JOURNAL_PROGRESS_CAPABILITY = new ResourceLocation(RiftInitialize.MODID, "playerjournalprogress");

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        //if (!(event.getObject() instanceof EntityLivingBase)) return;
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(PLAYER_TAMED_CREATURES_CAPABILITY, new PlayerTamedCreaturesProvider());
            event.addCapability(PLAYER_JOURNAL_PROGRESS_CAPABILITY, new PlayerJournalProgressProvider());
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
    public void onPlayerClone(PlayerEvent.Clone event) {
        EntityPlayer player = event.getEntityPlayer();

        //replicate tamed creatures
        IPlayerTamedCreatures tamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        IPlayerTamedCreatures oldTamedCreatures = event.getOriginal().getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        tamedCreatures.setPartyNBT(oldTamedCreatures.getPartyNBT());
        tamedCreatures.setBoxNBT(oldTamedCreatures.getBoxNBT());
        tamedCreatures.setMaxPartySize(oldTamedCreatures.getMaxPartySize());
        tamedCreatures.setLastSelected(oldTamedCreatures.getLastSelected());

        //replicate journal progress
        IPlayerJournalProgress journalProgress = player.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
        IPlayerJournalProgress oldJournalProgress = event.getOriginal().getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);

        journalProgress.setUnlockedCreatures(oldJournalProgress.getUnlockedCreatures());
    }
}
