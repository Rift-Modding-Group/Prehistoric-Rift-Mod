package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreaturePart;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftRemoveCreature extends AbstractMessage<RiftRemoveCreature> {
    private int entityId;
    private boolean isCreature; //true if creature, false if hitbox

    public RiftRemoveCreature() {}

    public RiftRemoveCreature(Entity entity, boolean isCreature) {
        this.entityId = entity.getEntityId();
        this.isCreature = isCreature;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.isCreature = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeBoolean(this.isCreature);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftRemoveCreature message, EntityPlayer messagePlayer, MessageContext messageContext) {
        if (message.isCreature) {
            RiftCreature creature = (RiftCreature) messagePlayer.world.getEntityByID(message.entityId);
            if (creature != null) messagePlayer.world.removeEntityDangerously(creature);
        }
        else {
            RiftCreaturePart creaturePart = (RiftCreaturePart) messagePlayer.world.getEntityByID(message.entityId);
            if (creaturePart != null) messagePlayer.world.removeEntityDangerously(creaturePart);
        }
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftRemoveCreature message, EntityPlayer messagePlayer, MessageContext messageContext) {
        if (message.isCreature) {
            RiftCreature creature = (RiftCreature) messagePlayer.world.getEntityByID(message.entityId);
            if (creature != null) messagePlayer.world.removeEntityDangerously(creature);
        }
        else {
            RiftCreaturePart creaturePart = (RiftCreaturePart) messagePlayer.world.getEntityByID(message.entityId);
            if (creaturePart != null) messagePlayer.world.removeEntityDangerously(creaturePart);
        }
    }
}
