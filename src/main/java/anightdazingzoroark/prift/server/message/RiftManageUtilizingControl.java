package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftManageUtilizingControl extends AbstractMessage<RiftManageUtilizingControl> {
    private int creatureId;
    private int control;
    private boolean isUsing;

    public RiftManageUtilizingControl() {}

    public RiftManageUtilizingControl(RiftCreature creature, int control, boolean isUsing) {
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
    public void onClientReceived(Minecraft client, RiftManageUtilizingControl message, EntityPlayer player, MessageContext messageContext) {

    }

    @Override
    public void onServerReceived(MinecraftServer server, RiftManageUtilizingControl message, EntityPlayer player, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature) player.world.getEntityByID(message.creatureId);
        switch (message.control) {
            case 0:
                creature.setUsingLeftClick(message.isUsing);
                break;
            case 1:
                creature.setUsingRightClick(message.isUsing);
                break;
            case 2:
                creature.setUsingSpacebar(message.isUsing);
                break;
        }
    }
}
