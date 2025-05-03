package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftSetWorkstation implements IMessage {
    private int creatureId;
    private boolean startUse;

    public RiftSetWorkstation() {}

    public RiftSetWorkstation(RiftCreature creature, boolean startUse) {
        this.creatureId = creature.getEntityId();
        this.startUse = startUse;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.startUse = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeBoolean(this.startUse);
    }

    public static class Handler implements IMessageHandler<RiftSetWorkstation, IMessage> {
        @Override
        public IMessage onMessage(RiftSetWorkstation message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftSetWorkstation message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            RiftCreature creature = (RiftCreature) playerEntity.world.getEntityByID(message.creatureId);
            if (creature.getOwner().equals(playerEntity)) {
                if (message.startUse) {
                    creature.setSitting(false);
                    ClientProxy.settingCreatureWorkstation = true;
                    ClientProxy.creatureIdForWorkstation = message.creatureId;
                    playerEntity.sendStatusMessage(new TextComponentTranslation("action.set_creature_workstation_start"), false);
                }
                else ((IWorkstationUser) creature).clearWorkstation(false);
            }
        }
    }
}
