package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.client.newui.data.PlayerGuiFactory;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftOpenPartyScreen extends RiftLibMessage<RiftOpenPartyScreen> {
    private int playerId;

    public RiftOpenPartyScreen() {}

    public RiftOpenPartyScreen(EntityPlayer player) {
        this.playerId = player.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        this.playerId = byteBuf.readInt();
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeInt(this.playerId);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftOpenPartyScreen message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) minecraftServer.getEntityWorld().getEntityByID(message.playerId);
        PlayerGuiFactory.INSTANCE.open(player);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftOpenPartyScreen riftOpenPartyScreen, EntityPlayer entityPlayer, MessageContext messageContext) {

    }
}
