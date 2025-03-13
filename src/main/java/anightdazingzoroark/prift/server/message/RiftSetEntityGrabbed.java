package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.INonPotionEffects;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftSetEntityGrabbed implements IMessage {
    private int entityId;
    private boolean isGrabbed;

    public RiftSetEntityGrabbed() {}

    public RiftSetEntityGrabbed(Entity entity, boolean isGrabbed) {
        this.entityId = entity.getEntityId();
        this.isGrabbed = isGrabbed;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.isGrabbed = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeBoolean(this.isGrabbed);
    }

    public static class Handler implements IMessageHandler<RiftSetEntityGrabbed, IMessage> {
        @Override
        public IMessage onMessage(RiftSetEntityGrabbed message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftSetEntityGrabbed message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayerMP messagePlayer = ctx.getServerHandler().player;
                Entity grabbedEntity = messagePlayer.world.getEntityByID(message.entityId);
                INonPotionEffects nonPotionEffects = grabbedEntity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);

                nonPotionEffects.setGrabbed(message.isGrabbed);
            }
            else if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;
                Entity grabbedEntity = messagePlayer.world.getEntityByID(message.entityId);
                INonPotionEffects nonPotionEffects = grabbedEntity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);

                nonPotionEffects.setGrabbed(message.isGrabbed);
            }
        }
    }
}
