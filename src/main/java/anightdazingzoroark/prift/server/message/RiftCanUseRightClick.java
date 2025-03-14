package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftCanUseRightClick implements IMessage {
    private int creatureId;
    private boolean value;

    public RiftCanUseRightClick() {}

    public RiftCanUseRightClick(RiftCreature creature, boolean value) {
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

    public static class Handler implements IMessageHandler<RiftCanUseRightClick, IMessage> {
        @Override
        public IMessage onMessage(RiftCanUseRightClick message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftCanUseRightClick message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            World world = playerEntity.world;
            RiftCreature interacted = (RiftCreature) world.getEntityByID(message.creatureId);

            if (!world.isRemote) interacted.setCanUseRightClick(message.value);
        }
    }
}
