package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftSetPartyLastSelected extends AbstractMessage<RiftSetPartyLastSelected> {
    private int playerId;
    private int lastSelected;

    public RiftSetPartyLastSelected() {}

    public RiftSetPartyLastSelected(EntityPlayer player, int lastSelected) {
        this.playerId = player.getEntityId();
        this.lastSelected = lastSelected;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.lastSelected = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.lastSelected);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftSetPartyLastSelected message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        playerTamedCreatures.setLastSelected(message.lastSelected);
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftSetPartyLastSelected message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        playerTamedCreatures.setLastSelected(message.lastSelected);
    }
}
