package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftForceSyncBoxSizeLevel extends AbstractMessage<RiftForceSyncBoxSizeLevel> {
    private int playerId;
    private int boxSizeLevel;

    public RiftForceSyncBoxSizeLevel() {}

    public RiftForceSyncBoxSizeLevel(EntityPlayer player) {
        this(player, -1);
    }

    public RiftForceSyncBoxSizeLevel(EntityPlayer player, int boxSizeLevel) {
        this.playerId = player.getEntityId();
        this.boxSizeLevel = boxSizeLevel;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.boxSizeLevel = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.boxSizeLevel);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftForceSyncBoxSizeLevel message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        if (message.boxSizeLevel < 0) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncBoxSizeLevel(player, playerTamedCreatures.getBoxSizeLevel()));
        else playerTamedCreatures.setBoxSizeLevel(message.boxSizeLevel);
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftForceSyncBoxSizeLevel message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        if (message.boxSizeLevel < 0) RiftMessages.WRAPPER.sendToAll(new RiftForceSyncBoxSizeLevel(player, playerTamedCreatures.getBoxSizeLevel()));
        else playerTamedCreatures.setBoxSizeLevel(message.boxSizeLevel);
    }
}
