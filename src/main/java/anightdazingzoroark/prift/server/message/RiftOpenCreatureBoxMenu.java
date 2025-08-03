package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.server.RiftGui;
import anightdazingzoroark.prift.server.ServerProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftOpenCreatureBoxMenu implements IMessage {
    private int playerId;
    private int posX;
    private int posY;
    private int posZ;

    public RiftOpenCreatureBoxMenu() {}

    public RiftOpenCreatureBoxMenu(EntityPlayer player, BlockPos pos) {
        this.playerId = player.getEntityId();
        this.posX = pos.getX();
        this.posY = pos.getY();
        this.posZ = pos.getZ();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.posX = buf.readInt();
        this.posY = buf.readInt();
        this.posZ = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.posX);
        buf.writeInt(this.posY);
        buf.writeInt(this.posZ);
    }

    public static class Handler implements IMessageHandler<RiftOpenCreatureBoxMenu, IMessage> {
        @Override
        public IMessage onMessage(RiftOpenCreatureBoxMenu message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftOpenCreatureBoxMenu message, MessageContext ctx) {
            EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

            EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
            ClientProxy.creatureBoxBlockPos = new BlockPos(message.posX, message.posY, message.posZ);
            player.openGui(RiftInitialize.instance, RiftGui.GUI_CREATURE_BOX, messagePlayer.world, -1, -1, -1);
        }
    }
}
