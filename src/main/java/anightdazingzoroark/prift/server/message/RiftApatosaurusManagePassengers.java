package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.Apatosaurus;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftApatosaurusManagePassengers implements IMessage {
    private int apatoId;

    public RiftApatosaurusManagePassengers() {}

    public RiftApatosaurusManagePassengers(Apatosaurus apatosaurus) {
        this.apatoId = apatosaurus.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.apatoId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.apatoId);
    }

    public static class Handler implements IMessageHandler<RiftApatosaurusManagePassengers, IMessage> {
        @Override
        public IMessage onMessage(RiftApatosaurusManagePassengers message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftApatosaurusManagePassengers message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            World world = playerEntity.world;
            Apatosaurus apatosaurus = (Apatosaurus) world.getEntityByID(message.apatoId);
            apatosaurus.addPassengersManual();
        }
    }
}
