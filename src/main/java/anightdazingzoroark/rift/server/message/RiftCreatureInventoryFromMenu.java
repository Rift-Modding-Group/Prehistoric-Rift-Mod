package anightdazingzoroark.rift.server.message;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.server.ServerProxy;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftCreatureInventoryFromMenu implements IMessage {
    private int creatureId;

    public RiftCreatureInventoryFromMenu() {}

    public RiftCreatureInventoryFromMenu(RiftCreature creature) {
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

    public static class Handler implements IMessageHandler<RiftCreatureInventoryFromMenu, IMessage> {
        @Override
        public IMessage onMessage(RiftCreatureInventoryFromMenu message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftCreatureInventoryFromMenu message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            World world = playerEntity.getEntityWorld();
            RiftCreature interacted = (RiftCreature) playerEntity.world.getEntityByID(message.creatureId);

            if (world != null) {
                playerEntity.openGui(RiftInitialize.instance, ServerProxy.GUI_CREATURE_INVENTORY, world, interacted.getEntityId(), 0, 0);
            }
        }
    }
}
