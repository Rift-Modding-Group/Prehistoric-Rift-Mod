package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.RiftGui;
import anightdazingzoroark.prift.server.ServerProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftOpenInventoryFromMenu implements IMessage {
    private int creatureId;

    public RiftOpenInventoryFromMenu() {}

    public RiftOpenInventoryFromMenu(int creatureId) {
        this.creatureId = creatureId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
    }

    public static class Handler implements IMessageHandler<RiftOpenInventoryFromMenu, IMessage> {
        @Override
        public IMessage onMessage(RiftOpenInventoryFromMenu message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftOpenInventoryFromMenu message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            World world = playerEntity.getEntityWorld();
            playerEntity.openGui(RiftInitialize.instance, RiftGui.GUI_CREATURE_INVENTORY, world, message.creatureId, 0, 0);
        }
    }
}
