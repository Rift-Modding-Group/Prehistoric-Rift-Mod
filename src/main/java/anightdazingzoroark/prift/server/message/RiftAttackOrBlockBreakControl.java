package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftAttackOrBlockBreakControl implements IMessage {
    private int creatureId;
    private boolean value;

    public RiftAttackOrBlockBreakControl() {}

    public RiftAttackOrBlockBreakControl(RiftCreature creature, boolean value) {
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

    public static class Handler implements IMessageHandler<RiftAttackOrBlockBreakControl, IMessage> {
        @Override
        public IMessage onMessage(RiftAttackOrBlockBreakControl message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftAttackOrBlockBreakControl message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            RiftCreature creature = (RiftCreature) playerEntity.world.getEntityByID(message.creatureId);

            if (creature != null) {
                if (message.value && creature.canSetBlockBreakMode()) {
                    creature.setBlockBreakMode(!creature.inBlockBreakMode());
                    creature.setCanSetBlockBreakMode(false);
                }
                else if (!message.value && !creature.canSetBlockBreakMode()) creature.setCanSetBlockBreakMode(true);
            }
        }
    }
}
