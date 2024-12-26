package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class RiftRemoveAfterSendToBox implements IMessage {
    private int creatureId;
    private UUID creatureUUID;
    private boolean useUUID;

    public RiftRemoveAfterSendToBox() {}

    public RiftRemoveAfterSendToBox(RiftCreature creature, boolean useUUID) {
        this.creatureId = creature.getEntityId();
        this.creatureUUID = creature.getUniqueID();
        this.useUUID = useUUID;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        long mostSigBits = buf.readLong();
        long leastSigBits = buf.readLong();
        this.creatureUUID = new UUID(mostSigBits, leastSigBits);
        this.useUUID = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeLong(this.creatureUUID.getMostSignificantBits());
        buf.writeLong(this.creatureUUID.getLeastSignificantBits());
        buf.writeBoolean(this.useUUID);
    }

    public static class Handler implements IMessageHandler<RiftRemoveAfterSendToBox, IMessage> {
        @Override
        public IMessage onMessage(RiftRemoveAfterSendToBox message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftRemoveAfterSendToBox message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                if (message.useUUID) {
                    RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, message.creatureUUID);
                    if (creature != null) RiftUtil.removeCreature(creature);
                }
                else {
                    RiftCreature creature = (RiftCreature)messagePlayer.world.getEntityByID(message.creatureId);
                    if (creature != null) RiftUtil.removeCreature(creature);
                }
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                if (message.useUUID) {
                    RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, message.creatureUUID);
                    if (creature != null) RiftUtil.removeCreature(creature);
                }
                else {
                    RiftCreature creature = (RiftCreature)messagePlayer.world.getEntityByID(message.creatureId);
                    if (creature != null) RiftUtil.removeCreature(creature);
                }
            }
        }
    }
}
