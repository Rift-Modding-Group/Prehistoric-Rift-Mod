package anightdazingzoroark.rift.server.message;

import anightdazingzoroark.rift.server.entity.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftMountControl implements IMessage {
    private int creatureId;
    private int control; //0 is for left click, 1 is for right click

    public RiftMountControl() {}

    public RiftMountControl(RiftCreature creature, int control) {
        this.creatureId = creature.getEntityId();
        this.control = control;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.control = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeInt(this.control);
    }

    public static class Handler implements IMessageHandler<RiftMountControl, IMessage> {
        @Override
        public IMessage onMessage(RiftMountControl message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftMountControl message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            World world = playerEntity.getEntityWorld();
            RiftCreature creature = (RiftCreature)world.getEntityByID(message.creatureId);

            switch (message.control) {
                case 0:
                    creature.controlAttack();
                    break;
                case 1:
                    break;
            }
        }
    }
}
