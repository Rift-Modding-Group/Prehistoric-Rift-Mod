package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class RiftTeleportPartyMemToPlayer implements IMessage {
    private UUID creatureUUID;

    public RiftTeleportPartyMemToPlayer() {}

    public RiftTeleportPartyMemToPlayer(RiftCreature creature) {
        this.creatureUUID = creature.getUniqueID();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        long mostSigBits = buf.readLong();
        long leastSigBits = buf.readLong();
        this.creatureUUID = new UUID(mostSigBits, leastSigBits);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.creatureUUID.getMostSignificantBits());
        buf.writeLong(this.creatureUUID.getLeastSignificantBits());
    }

    public static class Handler implements IMessageHandler<RiftTeleportPartyMemToPlayer, IMessage> {
        @Override
        public IMessage onMessage(RiftTeleportPartyMemToPlayer message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftTeleportPartyMemToPlayer message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            RiftCreature partyMember = (RiftCreature) RiftUtil.getEntityFromUUID(playerEntity.world, message.creatureUUID);
            EntityPlayer owner = (EntityPlayer) partyMember.getOwner();
            if (owner != null) partyMember.setPosition(owner.posX, owner.posY, owner.posZ);
        }
    }
}
