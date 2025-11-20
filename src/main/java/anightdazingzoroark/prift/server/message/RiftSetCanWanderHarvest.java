package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IHarvestWhenWandering;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftSetCanWanderHarvest extends RiftLibMessage<RiftSetCanWanderHarvest> {
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
    public void executeOnServer(MinecraftServer minecraftServer, RiftSetCanWanderHarvest message, EntityPlayer messagePlayer, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature) messagePlayer.world.getEntityByID(message.creatureId);
        IHarvestWhenWandering harvestWanderer = (IHarvestWhenWandering) creature;
        if (harvestWanderer == null) return;
        harvestWanderer.setCanHarvest(message.value);
        String messageToSend = message.value ? "action.creature_start_harvesting" : "action.creature_stop_harvesting";
        ((EntityPlayer) creature.getOwner()).sendStatusMessage(new TextComponentTranslation(messageToSend), false);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftSetCanWanderHarvest riftSetCanWanderHarvest, EntityPlayer entityPlayer, MessageContext messageContext) {}
}
