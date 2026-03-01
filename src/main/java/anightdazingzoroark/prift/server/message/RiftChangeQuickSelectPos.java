package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyHelper;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyProperties;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftChangeQuickSelectPos extends RiftLibMessage<RiftChangeQuickSelectPos> {
    private int playerId;
    //true is up or prev, false is down or forwards
    private boolean upOrDown;

    public RiftChangeQuickSelectPos() {}

    public RiftChangeQuickSelectPos(EntityPlayer player, boolean upOrDown) {
        this.playerId = player.getEntityId();
        this.upOrDown = upOrDown;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.upOrDown = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeBoolean(this.upOrDown);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftChangeQuickSelectPos message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) server.getEntityWorld().getEntityByID(message.playerId);
        if (player == null) return;

        PlayerPartyProperties playerPartyProperties = PlayerPartyHelper.getPlayerParty(player);
        if (message.upOrDown) playerPartyProperties.prevQuickSelectPos();
        else playerPartyProperties.nextQuickSelectPos();
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftChangeQuickSelectPos message, EntityPlayer messagePlayer, MessageContext messageContext) {

    }
}
