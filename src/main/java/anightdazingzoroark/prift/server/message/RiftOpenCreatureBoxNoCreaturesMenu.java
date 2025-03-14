package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.server.RiftGui;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.enums.PopupFromCreatureBox;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftOpenCreatureBoxNoCreaturesMenu implements IMessage {
    private int playerId;

    public RiftOpenCreatureBoxNoCreaturesMenu() {}

    public RiftOpenCreatureBoxNoCreaturesMenu(EntityPlayer player) {
        this.playerId = player.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
    }

    public static class Handler implements IMessageHandler<RiftOpenCreatureBoxNoCreaturesMenu, IMessage> {
        @Override
        public IMessage onMessage(RiftOpenCreatureBoxNoCreaturesMenu message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftOpenCreatureBoxNoCreaturesMenu message, MessageContext ctx) {
            EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

            EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
            ClientProxy.popupFromRadial = PopupFromCreatureBox.NO_CREATURES;
            player.openGui(RiftInitialize.instance, RiftGui.GUI_MENU_FROM_CREATURE_BOX, messagePlayer.world, 0, 0, 0);
        }
    }
}
