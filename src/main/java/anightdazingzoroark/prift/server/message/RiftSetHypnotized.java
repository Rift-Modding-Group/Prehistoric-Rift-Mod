package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.INonPotionEffects;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsProvider;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftSetHypnotized implements IMessage {
    private int entityId;
    private int hypnotizerId;

    public RiftSetHypnotized() {}

    public RiftSetHypnotized(EntityCreature entity, RiftCreature hypnotizer) {
        this.entityId = entity.getEntityId();
        this.hypnotizerId = hypnotizer != null ? hypnotizer.getEntityId() : -1;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.hypnotizerId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.hypnotizerId);
    }

    public static class Handler implements IMessageHandler<RiftSetHypnotized, IMessage> {
        @Override
        public IMessage onMessage(RiftSetHypnotized message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftSetHypnotized message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityCreature entity = (EntityCreature) messagePlayer.world.getEntityByID(message.entityId);
                RiftCreature hypnotizer = (RiftCreature) messagePlayer.world.getEntityByID(message.hypnotizerId);
                INonPotionEffects nonPotionEffects = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);

                if (nonPotionEffects != null) nonPotionEffects.hypnotize(hypnotizer);
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                EntityCreature entity = (EntityCreature) messagePlayer.world.getEntityByID(message.entityId);
                INonPotionEffects nonPotionEffects = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);

                if (nonPotionEffects != null) nonPotionEffects.hypnotize();
            }
        }
    }
}
