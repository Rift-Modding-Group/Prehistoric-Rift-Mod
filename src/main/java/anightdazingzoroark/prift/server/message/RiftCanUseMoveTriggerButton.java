package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftCanUseMoveTriggerButton implements IMessage {
    private int creatureId;
    private int control; //0 is move 1 or left click, 1 is move 2 or right click, 2 is move 3 or middle click
    private boolean value;

    public RiftCanUseMoveTriggerButton() {}

    public RiftCanUseMoveTriggerButton(RiftCreature creature, int control, boolean value) {
        this.creatureId = creature.getEntityId();
        this.control = control;
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.control = buf.readInt();
        this.value = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeInt(this.control);
        buf.writeBoolean(this.value);
    }

    public static class Handler implements IMessageHandler<RiftCanUseMoveTriggerButton, IMessage> {
        @Override
        public IMessage onMessage(RiftCanUseMoveTriggerButton message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftCanUseMoveTriggerButton message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            World world = playerEntity.world;
            RiftCreature interacted = (RiftCreature) world.getEntityByID(message.creatureId);

            if (!world.isRemote) {
                switch (message.control) {
                    case 0:
                        interacted.setCanUseLeftClick(message.value);
                        break;
                    case 1:
                        interacted.setCanUseRightClick(message.value);
                        break;
                    case 2:
                        interacted.setCanUseMiddleClick(message.value);
                        break;
                }
            }
        }
    }
}
