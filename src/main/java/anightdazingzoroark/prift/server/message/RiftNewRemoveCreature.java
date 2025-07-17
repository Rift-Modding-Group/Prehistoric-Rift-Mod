package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Arrays;

public class RiftNewRemoveCreature implements IMessage {
    private int entityId;

    public RiftNewRemoveCreature() {}

    public RiftNewRemoveCreature(Entity entity) {
        this.entityId = entity.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
    }

    public static class Handler implements IMessageHandler<RiftNewRemoveCreature, IMessage> {
        @Override
        public IMessage onMessage(RiftNewRemoveCreature message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftNewRemoveCreature message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                RiftCreature creature = (RiftCreature) messagePlayer.world.getEntityByID(message.entityId);

                System.out.println(Arrays.toString(creature.hitboxArray));
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                RiftCreature creature = (RiftCreature) messagePlayer.world.getEntityByID(message.entityId);

                System.out.println(Arrays.toString(creature.hitboxArray));
            }
        }
    }
}
