package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftForceSyncLastSelected extends AbstractMessage<RiftForceSyncLastSelected> {
    private int playerId;
    private int lastSelected;

    public RiftForceSyncLastSelected() {}

    public RiftForceSyncLastSelected(EntityPlayer player) {
        this(player, -1);
    }

    public RiftForceSyncLastSelected(EntityPlayer player, int lastSelected) {
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
    public void onClientReceived(Minecraft minecraft, RiftForceSyncLastSelected message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        if (message.lastSelected < 0) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncLastSelected(player, playerTamedCreatures.getLastSelected()));
        else playerTamedCreatures.setLastSelected(message.lastSelected);
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftForceSyncLastSelected message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        if (message.lastSelected < 0) RiftMessages.WRAPPER.sendToAll(new RiftForceSyncLastSelected(player, playerTamedCreatures.getLastSelected()));
        else playerTamedCreatures.setLastSelected(message.lastSelected);
    }
}
