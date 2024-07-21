package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IHarvestWhenWandering;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftSetCanWanderHarvest extends AbstractMessage<RiftSetCanWanderHarvest> {
    private int creatureId;
    private boolean value;

    public RiftSetCanWanderHarvest() {}

    public RiftSetCanWanderHarvest(RiftCreature creature, boolean value) {
        this.creatureId = creature.getEntityId();
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.value = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeBoolean(this.value);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftSetCanWanderHarvest message, EntityPlayer entityPlayer, MessageContext messageContext) {

    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftSetCanWanderHarvest message, EntityPlayer entityPlayer, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature)entityPlayer.world.getEntityByID(message.creatureId);
        IHarvestWhenWandering harvestWanderer = (IHarvestWhenWandering) creature;
        harvestWanderer.setCanHarvest(message.value);
        String messageToSend = message.value ? "action.creature_start_harvesting" : "action.creature_stop_harvesting";
        ((EntityPlayer) creature.getOwner()).sendStatusMessage(new TextComponentTranslation(messageToSend), false);
    }
}
