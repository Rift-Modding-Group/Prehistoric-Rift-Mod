package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftMultipartInteract implements IMessage {
    private int creatureId;

    public RiftMultipartInteract() {}

    public RiftMultipartInteract(RiftCreature creature) {
        this.creatureId = creature.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
    }

    public static class Handler implements IMessageHandler<RiftMultipartInteract, IMessage> {
        @Override
        public IMessage onMessage(RiftMultipartInteract message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftMultipartInteract message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            if (playerEntity.world != null) {
                RiftCreature creature = (RiftCreature) playerEntity.world.getEntityByID(message.creatureId);
                if (creature != null) {
                    double dist = playerEntity.getDistance(creature);
                    if (dist < 128) {
                        creature.processInteract(playerEntity, EnumHand.MAIN_HAND);
                        creature.processInitialInteract(playerEntity, EnumHand.MAIN_HAND);
                    }
                }
            }
        }
    }
}
