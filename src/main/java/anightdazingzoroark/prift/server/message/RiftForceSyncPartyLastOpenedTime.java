package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftForceSyncPartyLastOpenedTime extends AbstractMessage<RiftForceSyncPartyLastOpenedTime> {
    private int playerId;
    private int partyLastOpenedTime;

    public RiftForceSyncPartyLastOpenedTime() {}

    public RiftForceSyncPartyLastOpenedTime(EntityPlayer player) {
        this(player, -1);
    }

    public RiftForceSyncPartyLastOpenedTime(EntityPlayer player, int partyLastOpenedTime) {
        this.playerId = player.getEntityId();
        this.partyLastOpenedTime = partyLastOpenedTime;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.partyLastOpenedTime = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.partyLastOpenedTime);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftForceSyncPartyLastOpenedTime message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        if (message.partyLastOpenedTime < 0) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncPartyLastOpenedTime(player, playerTamedCreatures.getPartyLastOpenedTime()));
        else playerTamedCreatures.setPartyLastOpenedTime(message.partyLastOpenedTime);
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftForceSyncPartyLastOpenedTime message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        if (message.partyLastOpenedTime < 0) RiftMessages.WRAPPER.sendToAll(new RiftForceSyncPartyLastOpenedTime(player, playerTamedCreatures.getPartyLastOpenedTime()));
        else playerTamedCreatures.setPartyLastOpenedTime(message.partyLastOpenedTime);
    }
}
