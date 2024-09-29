package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftRemoveAfterSendToBox extends AbstractMessage<RiftRemoveAfterSendToBox> {
    private int creatureId;

    public RiftRemoveAfterSendToBox() {}

    public RiftRemoveAfterSendToBox(RiftCreature creature) {
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

    @Override
    public void onClientReceived(Minecraft minecraft, RiftRemoveAfterSendToBox message, EntityPlayer player, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature)player.world.getEntityByID(message.creatureId);
        if (creature != null) {
            //for removing hitboxes
            creature.setDead();
            creature.setHealth(0);
            creature.updateParts();
        }
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftRemoveAfterSendToBox message, EntityPlayer player, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature)player.world.getEntityByID(message.creatureId);
        if (creature != null) {
            //for removing hitboxes
            creature.setDead();
            creature.setHealth(0);
            creature.updateParts();
        }
    }
}
