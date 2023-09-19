package anightdazingzoroark.rift.server.message;

import anightdazingzoroark.rift.server.entity.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftManageCanUseRightClick implements IMessage {
    private int creatureId;
    private boolean canUseRightClick;

    public RiftManageCanUseRightClick() {}

    public RiftManageCanUseRightClick(RiftCreature creature, boolean canUseRightClick) {
        this.creatureId = creature.getEntityId();
        this.canUseRightClick = canUseRightClick;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.canUseRightClick = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeBoolean(this.canUseRightClick);
    }

    public static class Handler implements IMessageHandler<RiftManageCanUseRightClick, IMessage> {
        @Override
        public IMessage onMessage(RiftManageCanUseRightClick message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftManageCanUseRightClick message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            World world = playerEntity.getEntityWorld();
            RiftCreature interacted = (RiftCreature) playerEntity.world.getEntityByID(message.creatureId);

            if (world != null && !world.isRemote) interacted.setCanUseRightClick(message.canUseRightClick);
        }
    }
}
