package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftManageCanUseControl implements IMessage {
    private int creatureId;
    private int control; //0 for left click, 1 for right click, 2 for spacebar
    private boolean canUseControl;

    public RiftManageCanUseControl() {}

    public RiftManageCanUseControl(RiftCreature creature, int control, boolean canUseControl) {
        this.creatureId = creature.getEntityId();
        this.control = control;
        this.canUseControl = canUseControl;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.control = buf.readInt();
        this.canUseControl = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeInt(this.control);
        buf.writeBoolean(this.canUseControl);
    }

    public static class Handler implements IMessageHandler<RiftManageCanUseControl, IMessage> {
        @Override
        public IMessage onMessage(RiftManageCanUseControl message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftManageCanUseControl message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            World world = playerEntity.world;
            RiftCreature interacted = (RiftCreature) world.getEntityByID(message.creatureId);

            if (!world.isRemote) {
                switch (message.control) {
                    case 0:
                        interacted.setCanUseLeftClick(message.canUseControl);
                        break;
                    case 1:
                        interacted.setCanUseRightClick(message.canUseControl);
                        break;
                    case 2:
                        interacted.setCanUseSpacebar(message.canUseControl);
                        break;
                }
            }
        }
    }
}
