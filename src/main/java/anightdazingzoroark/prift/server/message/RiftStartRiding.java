package anightdazingzoroark.prift.server.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftStartRiding implements IMessage {
    private int entityId;

    public RiftStartRiding() {}

    public RiftStartRiding(EntityLiving entity) {
        this.entityId = entity.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
    }

    public static class Handler implements IMessageHandler<RiftStartRiding, IMessage> {
        @Override
        public IMessage onMessage(RiftStartRiding message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftStartRiding message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            World world = playerEntity.getEntityWorld();
            EntityLiving entity = (EntityLiving)world.getEntityByID(message.entityId);

            entity.getNavigator().clearPath();
            entity.setAttackTarget(null);
            playerEntity.startRiding(entity, true);
        }
    }
}
