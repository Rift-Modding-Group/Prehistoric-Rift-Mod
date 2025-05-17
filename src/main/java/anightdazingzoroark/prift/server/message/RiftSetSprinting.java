package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftSetSprinting implements IMessage {
    private int creatureId;
    private boolean value;

    public RiftSetSprinting() {}

    public RiftSetSprinting(RiftCreature creature, boolean value) {
        this.creatureId = creature.getEntityId();
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.value = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
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
                RiftCreature creature = (RiftCreature) messagePlayer.world.getEntityByID(message.creatureId);
                if (creature != null) creature.setSprinting(message.value);
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;
                RiftCreature creature = (RiftCreature) messagePlayer.world.getEntityByID(message.creatureId);
                if (creature != null) creature.setSprinting(message.value);
            }
        }
    }
}
