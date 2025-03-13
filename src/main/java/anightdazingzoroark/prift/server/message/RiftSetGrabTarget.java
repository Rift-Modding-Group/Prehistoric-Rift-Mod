package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftSetGrabTarget implements IMessage {
    private int grabberId;
    private int targetId;

    public RiftSetGrabTarget() {}

    public RiftSetGrabTarget(RiftCreature grabber, EntityLivingBase target) {
        this.grabberId = grabber.getEntityId();
        this.targetId = (target != null) ? target.getEntityId() : -1;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.grabberId = buf.readInt();
        this.targetId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.grabberId);
        buf.writeInt(this.targetId);
    }

    public static class Handler implements IMessageHandler<RiftSetGrabTarget, IMessage> {
        @Override
        public IMessage onMessage(RiftSetGrabTarget message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftSetGrabTarget message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            RiftCreature grabber = (RiftCreature) playerEntity.world.getEntityByID(message.grabberId);

            if (message.targetId != -1) {
                EntityLivingBase target = (EntityLivingBase) playerEntity.world.getEntityByID(message.targetId);
                grabber.setGrabVictim(target);
            }
            else grabber.setGrabVictim(null);
        }
    }
}
