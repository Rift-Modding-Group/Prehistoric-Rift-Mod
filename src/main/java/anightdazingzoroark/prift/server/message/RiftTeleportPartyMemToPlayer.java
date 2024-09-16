package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class RiftTeleportPartyMemToPlayer extends AbstractMessage<RiftTeleportPartyMemToPlayer> {
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

    @Override
    public void onClientReceived(Minecraft minecraft, RiftTeleportPartyMemToPlayer riftTeleportPartyMemToPlayer, EntityPlayer entityPlayer, MessageContext messageContext) {

    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftTeleportPartyMemToPlayer message, EntityPlayer player, MessageContext messageContext) {
        RiftCreature partyMember = (RiftCreature) RiftUtil.getEntityFromUUID(player.world, message.creatureUUID);
        EntityPlayer owner = (EntityPlayer) partyMember.getOwner();
        if (owner != null) partyMember.setPosition(owner.posX, owner.posY, owner.posZ);
    }
}
