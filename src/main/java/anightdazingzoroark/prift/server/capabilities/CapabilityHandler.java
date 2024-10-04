package anightdazingzoroark.prift.server.capabilities;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftUpdatePlayerTamedCreatures;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapabilityHandler {
    public static final ResourceLocation PLAYER_TAMED_CREATURES_CAPABILITY = new ResourceLocation(RiftInitialize.MODID, "playertamedcreatures");

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        //if (!(event.getObject() instanceof EntityLivingBase)) return;
        if (event.getObject() instanceof EntityPlayer) event.addCapability(PLAYER_TAMED_CREATURES_CAPABILITY, new PlayerTamedCreaturesProvider());
    }

    /*
    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!event.getEntity().world.isRemote && event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            IPlayerTamedCreatures tamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
            if (tamedCreatures == null) return;
            RiftMessages.WRAPPER.sendToAll(new RiftUpdatePlayerTamedCreatures(PlayerTamedCreaturesProvider.writeNBT(tamedCreatures, null), player));
        }
    }
    */

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!event.getEntity().world.isRemote && event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            IPlayerTamedCreatures tamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
            if (tamedCreatures == null) return;
            RiftMessages.WRAPPER.sendToAll(new RiftUpdatePlayerTamedCreatures(PlayerTamedCreaturesProvider.writeNBT(tamedCreatures, null), player));
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        EntityPlayer player = event.getEntityPlayer();
        IPlayerTamedCreatures tamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        IPlayerTamedCreatures oldTamedCreatures = event.getOriginal().getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        tamedCreatures.setPartyNBT(oldTamedCreatures.getPartyNBT());
        tamedCreatures.setBoxNBT(oldTamedCreatures.getBoxNBT());
        tamedCreatures.setMaxPartySize(oldTamedCreatures.getMaxPartySize());
        tamedCreatures.setLastSelected(oldTamedCreatures.getLastSelected());
    }
}
