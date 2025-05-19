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

public class RiftSetSprinting implements IMessage {
    private int entityId;
    private boolean value;

    public RiftSetSprinting() {}

    public RiftSetSprinting(EntityLivingBase creature, boolean value) {
        this.entityId = creature.getEntityId();
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.value = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeBoolean(this.value);
    }

    public static class Handler implements IMessageHandler<RiftSetSprinting, IMessage> {
        @Override
        public IMessage onMessage(RiftSetSprinting message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftSetSprinting message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;
                EntityLivingBase entityLivingBase = (EntityLivingBase) messagePlayer.world.getEntityByID(message.entityId);
                if (entityLivingBase != null) entityLivingBase.setSprinting(message.value);
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;
                EntityLivingBase entityLivingBase = (EntityLivingBase) messagePlayer.world.getEntityByID(message.entityId);
                if (entityLivingBase != null) entityLivingBase.setSprinting(message.value);
            }
        }
    }
}
