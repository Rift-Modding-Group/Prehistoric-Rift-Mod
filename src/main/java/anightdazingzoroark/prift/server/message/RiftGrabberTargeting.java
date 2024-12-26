package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IGrabber;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftGrabberTargeting implements IMessage {
    private int creatureId;
    private int targetId;

    public RiftGrabberTargeting() {}

    public RiftGrabberTargeting(RiftCreature creature, EntityLivingBase target) {
        this.creatureId = creature.getEntityId();
        this.targetId = target.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.targetId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeInt(this.targetId);
    }

    public static class Handler implements IMessageHandler<RiftGrabberTargeting, IMessage> {
        @Override
        public IMessage onMessage(RiftGrabberTargeting message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftGrabberTargeting message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            RiftCreature creature = (RiftCreature) playerEntity.world.getEntityByID(message.creatureId);
            EntityLivingBase target = (EntityLivingBase) playerEntity.world.getEntityByID(message.targetId);
            IGrabber grabber = (IGrabber) creature;

            target.setPosition(grabber.grabLocation().x, grabber.grabLocation().y, grabber.grabLocation().z);
            target.motionX = 0;
            target.motionY = 0;
            target.motionZ = 0;
        }
    }
}
