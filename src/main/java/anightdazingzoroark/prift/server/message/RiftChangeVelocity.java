package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftChangeVelocity extends AbstractMessage<RiftChangeVelocity> {
    private int creatureId;
    private boolean addMode;
    private double x;
    private double y;
    private double z;

    public RiftChangeVelocity() {}

    public RiftChangeVelocity(RiftCreature creature, boolean addMode, double x, double y, double z) {
        this.creatureId = creature.getEntityId();
        this.addMode = addMode;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.addMode = buf.readBoolean();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeBoolean(this.addMode);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftChangeVelocity message, EntityPlayer entityPlayer, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature) entityPlayer.world.getEntityByID(message.creatureId);
        if (message.addMode) {
            creature.motionX += message.x;
            creature.motionY += message.y;
            creature.motionZ += message.z;
        }
        else {
            creature.motionX = message.x;
            creature.motionY = message.y;
            creature.motionZ = message.z;
        }
        creature.velocityChanged = true;
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftChangeVelocity riftChangeVelocity, EntityPlayer entityPlayer, MessageContext messageContext) {

    }
}
