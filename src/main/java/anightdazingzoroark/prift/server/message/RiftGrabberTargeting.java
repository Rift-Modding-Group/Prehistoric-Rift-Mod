package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IGrabber;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftGrabberTargeting extends AbstractMessage<RiftGrabberTargeting> {
    private int creatureId;
    private int targetId;

    public RiftGrabberTargeting() {}

    public RiftGrabberTargeting(RiftCreature creature, EntityLivingBase target) {
        this.creatureId = creature.getEntityId();
        this.targetId = target.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.targetId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeInt(this.targetId);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftGrabberTargeting riftGrabberTargeting, EntityPlayer entityPlayer, MessageContext messageContext) {

    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftGrabberTargeting message, EntityPlayer entityPlayer, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature) entityPlayer.world.getEntityByID(message.creatureId);
        EntityLivingBase target = (EntityLivingBase) entityPlayer.world.getEntityByID(message.targetId);
        IGrabber grabber = (IGrabber) creature;

        target.setPosition(grabber.grabLocation().x, grabber.grabLocation().y, grabber.grabLocation().z);
        target.motionX = 0;
        target.motionY = 0;
        target.motionZ = 0;
    }
}
