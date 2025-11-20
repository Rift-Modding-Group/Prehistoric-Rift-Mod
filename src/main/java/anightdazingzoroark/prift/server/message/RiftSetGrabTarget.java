package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftSetGrabTarget extends RiftLibMessage<RiftSetGrabTarget> {
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
    public void executeOnServer(MinecraftServer minecraftServer, RiftSetGrabTarget message, EntityPlayer messagePlayer, MessageContext messageContext) {
        RiftCreature grabber = (RiftCreature) messagePlayer.world.getEntityByID(message.grabberId);
        if (grabber == null) return;

        if (message.targetId != -1) {
            EntityLivingBase target = (EntityLivingBase) messagePlayer.world.getEntityByID(message.targetId);
            grabber.setGrabVictim(target);
        }
        else grabber.setGrabVictim(null);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftSetGrabTarget riftSetGrabTarget, EntityPlayer entityPlayer, MessageContext messageContext) {}

    public static class Handler implements IMessageHandler<RiftSetGrabTarget, IMessage> {
        @Override
        public IMessage onMessage(RiftSetGrabTarget message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftSetGrabTarget message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            RiftCreature grabber = (RiftCreature) playerEntity.world.getEntityByID(message.grabberId);

            if (message.targetId != -1) {
                EntityLivingBase target = (EntityLivingBase) playerEntity.world.getEntityByID(message.targetId);
                grabber.setGrabVictim(target);
            }
            else grabber.setGrabVictim(null);
        }
    }
}
