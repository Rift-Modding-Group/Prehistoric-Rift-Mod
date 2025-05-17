package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftInitialize;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftShowParticlesOnClient implements IMessage {
    private String particleName;
    private int color;
    private double posX, posY, posZ;
    private double motionX, motionY, motionZ;

    public RiftShowParticlesOnClient() {}

    public RiftShowParticlesOnClient(String particleName, int color, double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
        this.particleName = particleName;
        this.color = color;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.particleName = ByteBufUtils.readUTF8String(buf);
        this.color = buf.readInt();
        this.posX = buf.readDouble();
        this.posY = buf.readDouble();
        this.posZ = buf.readDouble();
        this.motionX = buf.readDouble();
        this.motionY = buf.readDouble();
        this.motionZ = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.particleName);
        buf.writeInt(this.color);
        buf.writeDouble(this.posX);
        buf.writeDouble(this.posY);
        buf.writeDouble(this.posZ);
        buf.writeDouble(this.motionX);
        buf.writeDouble(this.motionY);
        buf.writeDouble(this.motionZ);
    }

    public static class Handler implements IMessageHandler<RiftShowParticlesOnClient, IMessage> {
        @Override
        public IMessage onMessage(RiftShowParticlesOnClient message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftShowParticlesOnClient message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                RiftInitialize.PROXY.spawnParticle(message.particleName, message.color, message.posX, message.posY, message.posZ, message.motionX, message.motionY, message.motionZ);
            }
        }
    }
}
