package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftCreatureBoxSetLastOpenedTime extends AbstractMessage<RiftCreatureBoxSetLastOpenedTime> {
    private int playerId;
    private int lastOpenedTime;

    public RiftCreatureBoxSetLastOpenedTime() {}

    public RiftCreatureBoxSetLastOpenedTime(EntityPlayer player, int lastOpenedTime) {
        this.playerId = player.getEntityId();
        this.lastOpenedTime = lastOpenedTime;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.lastOpenedTime = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.lastOpenedTime);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftCreatureBoxSetLastOpenedTime message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        playerTamedCreatures.setLastOpenedTime(message.lastOpenedTime);
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftCreatureBoxSetLastOpenedTime message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        playerTamedCreatures.setLastOpenedTime(message.lastOpenedTime);
    }
}
