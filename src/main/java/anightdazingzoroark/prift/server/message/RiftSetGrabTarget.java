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

public class RiftSetGrabTarget extends AbstractMessage<RiftSetGrabTarget> {
    private int grabberId;
    private int targetId;

    public RiftSetGrabTarget() {}

    public RiftSetGrabTarget(RiftCreature grabber, EntityLivingBase target) {
        this.grabberId = grabber.getEntityId();
        this.targetId = (target != null) ? target.getEntityId() : -1;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.grabberId = buf.readInt();
        this.targetId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.grabberId);
        buf.writeInt(this.targetId);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftSetGrabTarget message, EntityPlayer entityPlayer, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftSetGrabTarget message, EntityPlayer entityPlayer, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature) entityPlayer.world.getEntityByID(message.grabberId);
        IGrabber grabber = (IGrabber) creature;

        if (message.targetId != -1) {
            EntityLivingBase target = (EntityLivingBase) entityPlayer.world.getEntityByID(message.targetId);
            grabber.setGrabVictim(target);
        }
        else grabber.setGrabVictim(null);
    }
}
