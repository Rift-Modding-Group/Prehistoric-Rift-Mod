package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftHoverChangeControl extends AbstractMessage<RiftHoverChangeControl> {
    private int creatureId;
    private int control; //0 is ascend, 1 is descend
    private boolean isUsing;

    public RiftHoverChangeControl() {}

    public RiftHoverChangeControl(RiftCreature creature, int control, boolean isUsing) {
        this.creatureId = creature.getEntityId();
        this.control = control;
        this.isUsing = isUsing;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.control = buf.readInt();
        this.isUsing = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeInt(this.control);
        buf.writeBoolean(this.isUsing);
    }

    @Override
    public void onClientReceived(Minecraft client, RiftHoverChangeControl message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer server, RiftHoverChangeControl message, EntityPlayer player, MessageContext messageContext) {
        RiftWaterCreature creature = (RiftWaterCreature)player.world.getEntityByID(message.creatureId);
        if (creature.isInWater()) {
            if (message.control == 0) creature.setIsAscending(message.isUsing);
            else if (message.control == 1) creature.setIsDescending(message.isUsing);
        }
    }
}
