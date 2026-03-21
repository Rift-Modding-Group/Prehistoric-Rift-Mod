package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxHelper;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxProperties;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftSetBoxLastOpenedTime extends RiftLibMessage<RiftSetBoxLastOpenedTime> {
    private int playerId;
    private int newTime;

    public RiftSetBoxLastOpenedTime() {}

    public RiftSetBoxLastOpenedTime(EntityPlayer player, int newTime) {
        this.playerId = player.getEntityId();
        this.newTime = newTime;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.newTime = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.newTime);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftSetBoxLastOpenedTime message, EntityPlayer entityPlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) server.getEntityWorld().getEntityByID(message.playerId);
        if (player == null) return;

        PlayerCreatureBoxProperties creatureBoxProperties = PlayerCreatureBoxHelper.getPlayerCreatureBox(player);
        if (creatureBoxProperties == null) return;

        creatureBoxProperties.setLastTimeOpened(message.newTime);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftSetBoxLastOpenedTime message, EntityPlayer entityPlayer, MessageContext messageContext) {

    }
}
