package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.client.newui.data.PlayerGuiFactory;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftOpenPlayerScreen extends RiftLibMessage<RiftOpenPlayerScreen> {
    private int playerId;
    private String screenName;

    public RiftOpenPlayerScreen() {}

    public RiftOpenPlayerScreen(EntityPlayer player, String screenName) {
        this.playerId = player.getEntityId();
        this.screenName = screenName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.screenName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        ByteBufUtils.writeUTF8String(buf, this.screenName);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftOpenPlayerScreen message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) server.getEntityWorld().getEntityByID(message.playerId);
        PlayerGuiFactory.INSTANCE.setScreen(message.screenName).open(player);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftOpenPlayerScreen message, EntityPlayer messagePlayer, MessageContext messageContext) {

    }
}
