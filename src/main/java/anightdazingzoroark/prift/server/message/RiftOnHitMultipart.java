package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftOnHitMultipart extends AbstractMessage<RiftOnHitMultipart> {
    private int creatureId;

    public RiftOnHitMultipart() {}

    public RiftOnHitMultipart(RiftCreature creature) {
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
    public void onClientReceived(Minecraft client, RiftOnHitMultipart message, EntityPlayer player, MessageContext messageContext) {
        if (player.world != null) {
            RiftCreature creature = (RiftCreature) player.world.getEntityByID(message.creatureId);
            if (creature != null) {
                double dist = player.getDistance(creature);
                if (dist < 128) {
                    player.attackTargetEntityWithCurrentItem(creature);
                }
            }
        }
    }

    @Override
    public void onServerReceived(MinecraftServer server, RiftOnHitMultipart message, EntityPlayer player, MessageContext messageContext) {
        if (player.world != null) {
            RiftCreature creature = (RiftCreature) player.world.getEntityByID(message.creatureId);
            if (creature != null) {
                double dist = player.getDistance(creature);
                if (dist < 128) {
                    player.attackTargetEntityWithCurrentItem(creature);
                }
            }
        }
    }
}
