package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftSetEntityMotion implements IMessage {
    private int entityId;
    private double motionX;
    private double motionY;
    private double motionZ;

    public RiftSetEntityMotion() {}

    public RiftSetEntityMotion(EntityLivingBase entityLivingBase, double motionX, double motionZ) {
        this(entityLivingBase, motionX, entityLivingBase.motionY, motionZ);
    }

    public RiftSetEntityMotion(EntityLivingBase entityLivingBase, double motionX, double motionY, double motionZ) {
        this.entityId = entityLivingBase.getEntityId();
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.motionX = buf.readDouble();
        this.motionY = buf.readDouble();
        this.motionZ = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeDouble(this.motionX);
        buf.writeDouble(this.motionY);
        buf.writeDouble(this.motionZ);
    }

    public static class Handler implements IMessageHandler<RiftSetEntityMotion, IMessage> {
        @Override
        public IMessage onMessage(RiftSetEntityMotion message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftSetEntityMotion message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;
                EntityLivingBase entityLivingBase = (EntityLivingBase) messagePlayer.world.getEntityByID(message.entityId);
                if (entityLivingBase != null) entityLivingBase.setVelocity(message.motionX, message.motionY, message.motionZ);
            }
        }
    }
}
