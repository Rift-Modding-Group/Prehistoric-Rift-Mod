package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftHoverChangeControl implements IMessage {
    private int creatureId;
    private int control; //0 is ascend, 1 is descend
    private boolean isUsing;

    public RiftHoverChangeControl() {}

    public RiftHoverChangeControl(RiftCreature creature, int control, boolean isUsing) {
        this.creatureId = creature.getEntityId();
        this.control = control;
        this.isUsing = isUsing;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.control = buf.readInt();
        this.isUsing = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeInt(this.control);
        buf.writeBoolean(this.isUsing);
    }

    public static class Handler implements IMessageHandler<RiftHoverChangeControl, IMessage> {
        @Override
        public IMessage onMessage(RiftHoverChangeControl message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftHoverChangeControl message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            RiftWaterCreature creature = (RiftWaterCreature)playerEntity.world.getEntityByID(message.creatureId);
            if (creature.isInWater()) {
                if (message.control == 0) creature.setIsAscending(message.isUsing);
                else if (message.control == 1) creature.setIsDescending(message.isUsing);
            }
        }
    }
}
