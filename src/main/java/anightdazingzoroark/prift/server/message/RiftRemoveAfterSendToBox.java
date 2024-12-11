package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class RiftRemoveAfterSendToBox extends AbstractMessage<RiftRemoveAfterSendToBox> {
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

    @Override
    public void onClientReceived(Minecraft minecraft, RiftRemoveAfterSendToBox message, EntityPlayer player, MessageContext messageContext) {
        if (message.useUUID) {
            RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(player.world, message.creatureUUID);
            if (creature != null) RiftUtil.removeCreature(creature);
        }
        else {
            RiftCreature creature = (RiftCreature)player.world.getEntityByID(message.creatureId);
            if (creature != null) RiftUtil.removeCreature(creature);
        }
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftRemoveAfterSendToBox message, EntityPlayer player, MessageContext messageContext) {
        if (message.useUUID) {
            RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(player.world, message.creatureUUID);
            if (creature != null) RiftUtil.removeCreature(creature);
        }
        else {
            RiftCreature creature = (RiftCreature)player.world.getEntityByID(message.creatureId);
            if (creature != null) RiftUtil.removeCreature(creature);
        }
    }
}
