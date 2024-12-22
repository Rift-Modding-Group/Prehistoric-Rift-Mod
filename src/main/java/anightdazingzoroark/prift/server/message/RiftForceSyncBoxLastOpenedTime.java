package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftForceSyncBoxLastOpenedTime extends AbstractMessage<RiftForceSyncBoxLastOpenedTime> {
    private int playerId;
    private int boxLastOpenedTime;

    public RiftForceSyncBoxLastOpenedTime() {}

    public RiftForceSyncBoxLastOpenedTime(EntityPlayer player) {
        this(player, -1);
    }

    public RiftForceSyncBoxLastOpenedTime(EntityPlayer player, int boxLastOpenedTime) {
        this.playerId = player.getEntityId();
        this.boxLastOpenedTime = boxLastOpenedTime;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.boxLastOpenedTime = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.boxLastOpenedTime);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftForceSyncBoxLastOpenedTime message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        if (message.boxLastOpenedTime < 0) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncBoxLastOpenedTime(player, playerTamedCreatures.getBoxLastOpenedTime()));
        else playerTamedCreatures.setBoxLastOpenedTime(message.boxLastOpenedTime);
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftForceSyncBoxLastOpenedTime message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        if (message.boxLastOpenedTime < 0) RiftMessages.WRAPPER.sendToAll(new RiftForceSyncBoxLastOpenedTime(player, playerTamedCreatures.getBoxLastOpenedTime()));
        else playerTamedCreatures.setBoxLastOpenedTime(message.boxLastOpenedTime);
    }
}
