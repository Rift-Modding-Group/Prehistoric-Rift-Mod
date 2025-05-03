package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.INonPotionEffects;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftSetBleeding implements IMessage {
    private int entityId;
    private int strength;
    private int ticks;

    public RiftSetBleeding() {}

    public RiftSetBleeding(Entity entity, int strength, int ticks) {
        this.entityId = entity.getEntityId();
        this.strength = strength;
        this.ticks = ticks;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.strength = buf.readInt();
        this.ticks = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.strength);
        buf.writeInt(this.ticks);
    }

    public static class Handler implements IMessageHandler<RiftSetBleeding, IMessage> {
        @Override
        public IMessage onMessage(RiftSetBleeding message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftSetBleeding message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;
                INonPotionEffects nonPotionEffects = messagePlayer.world.getEntityByID(message.entityId).getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
                nonPotionEffects.setBleeding(message.strength, message.ticks);
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;
                INonPotionEffects nonPotionEffects = messagePlayer.world.getEntityByID(message.entityId).getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
                nonPotionEffects.setBleeding(message.strength, message.ticks);
            }
        }
    }
}
