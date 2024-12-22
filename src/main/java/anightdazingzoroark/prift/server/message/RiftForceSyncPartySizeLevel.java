package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftForceSyncPartySizeLevel extends AbstractMessage<RiftForceSyncPartySizeLevel> {
    private int playerId;
    private int partySizeLevel;

    public RiftForceSyncPartySizeLevel() {}

    public RiftForceSyncPartySizeLevel(EntityPlayer player) {
        this(player, -1);
    }

    public RiftForceSyncPartySizeLevel(EntityPlayer player, int partySizeLevel) {
        this.playerId = player.getEntityId();
        this.partySizeLevel = partySizeLevel;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.partySizeLevel = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.partySizeLevel);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftForceSyncPartySizeLevel message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        if (message.partySizeLevel < 0) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncPartySizeLevel(player, playerTamedCreatures.getPartySizeLevel()));
        else playerTamedCreatures.setPartySizeLevel(message.partySizeLevel);
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftForceSyncPartySizeLevel message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        if (message.partySizeLevel < 0) RiftMessages.WRAPPER.sendToAll(new RiftForceSyncPartySizeLevel(player, playerTamedCreatures.getPartySizeLevel()));
        else playerTamedCreatures.setPartySizeLevel(message.partySizeLevel);
    }
}
