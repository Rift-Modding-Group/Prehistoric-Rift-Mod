package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftManageUtilizingClick extends AbstractMessage<RiftManageUtilizingClick> {
    private int creatureId;
    private int mouse;
    private boolean isUsing;

    public RiftManageUtilizingClick() {}

    public RiftManageUtilizingClick(RiftCreature creature, int mouse, boolean isUsing) {
        this.creatureId = creature.getEntityId();
        this.mouse = mouse;
        this.isUsing = isUsing;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.mouse = buf.readInt();
        this.isUsing = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeInt(this.mouse);
        buf.writeBoolean(this.isUsing);
    }

    @Override
    public void onClientReceived(Minecraft client, RiftManageUtilizingClick message, EntityPlayer player, MessageContext messageContext) {

    }

    @Override
    public void onServerReceived(MinecraftServer server, RiftManageUtilizingClick message, EntityPlayer player, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature) player.world.getEntityByID(message.creatureId);
        if (message.mouse == 0) creature.setUsingLeftClick(message.isUsing);
        else creature.setUsingRightClick(message.isUsing);
    }
}
