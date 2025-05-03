package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftForceChangePos implements IMessage {
    private int creatureId;
    private double x;
    private double y;
    private double z;

    public RiftForceChangePos() {}

    public RiftForceChangePos(RiftCreature creature, double x, double y, double z) {
        this.creatureId = creature.getEntityId();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
    }

    public static class Handler implements IMessageHandler<RiftForceChangePos, IMessage> {
        @Override
        public IMessage onMessage(RiftForceChangePos message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftForceChangePos message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            RiftCreature creature = (RiftCreature)playerEntity.world.getEntityByID(message.creatureId);
            creature.setPosition(message.x, message.y, message.z);
        }
    }
}
