package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftMultipartInteract extends AbstractMessage<RiftMultipartInteract> {
    private int creatureId;

    public RiftMultipartInteract() {}

    public RiftMultipartInteract(RiftCreature creature) {
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
    public void onClientReceived(Minecraft client, RiftMultipartInteract message, EntityPlayer player, MessageContext messageContext) {
        if (player.world != null) {
            RiftCreature creature = (RiftCreature) player.world.getEntityByID(message.creatureId);
            if (creature != null) {
                double dist = player.getDistance(creature);
                if (dist < 128) {
                    creature.processInteract(player, EnumHand.MAIN_HAND);
                    creature.processInitialInteract(player, EnumHand.MAIN_HAND);
                }
            }
        }
    }

    @Override
    public void onServerReceived(MinecraftServer server, RiftMultipartInteract message, EntityPlayer player, MessageContext messageContext) {
        if (player.world != null) {
            RiftCreature creature = (RiftCreature) player.world.getEntityByID(message.creatureId);
            if (creature != null) {
                double dist = player.getDistance(creature);
                if (dist < 128) {
                    creature.processInteract(player, EnumHand.MAIN_HAND);
                    creature.processInitialInteract(player, EnumHand.MAIN_HAND);
                }
            }
        }
    }
}
