package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftIncrementClickUse extends AbstractMessage<RiftIncrementClickUse> {
    private int creatureId;
    private int mouse;

    public RiftIncrementClickUse() {}

    public RiftIncrementClickUse(RiftCreature creature, int mouse) {
        this.creatureId = creature.getEntityId();
        this.mouse = mouse;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.mouse = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeInt(this.mouse);
    }

    @Override
    public void onClientReceived(Minecraft client, RiftIncrementClickUse message, EntityPlayer player, MessageContext messageContext) {

    }

    @Override
    public void onServerReceived(MinecraftServer server, RiftIncrementClickUse message, EntityPlayer player, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature) player.world.getEntityByID(message.creatureId);

        if (message.mouse == 0) creature.setLeftClickUse(creature.getLeftClickUse() + 1);
        else if (message.mouse == 1) {
            if (creature.getEnergy() > 6) creature.setRightClickUse(creature.getRightClickUse() + 1);
        }
    }
}
