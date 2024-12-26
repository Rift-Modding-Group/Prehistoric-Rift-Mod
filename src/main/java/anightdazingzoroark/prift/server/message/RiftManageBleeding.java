package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.RiftDamage;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.INonPotionEffects;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftManageBleeding implements IMessage {
    private int entityId;
    private boolean isMoving;

    public RiftManageBleeding() {}

    public RiftManageBleeding(Entity entity, boolean isMoving) {
        this.entityId = entity.getEntityId();
        this.isMoving = isMoving;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.isMoving = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeBoolean(this.isMoving);
    }

    public static class Handler implements IMessageHandler<RiftManageBleeding, IMessage> {
        @Override
        public IMessage onMessage(RiftManageBleeding message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftManageBleeding message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;

            Entity entity = playerEntity.world.getEntityByID(message.entityId);
            INonPotionEffects nonPotionEffects = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
            if (message.isMoving) {
                nonPotionEffects.reduceBleedTick();
                if ((nonPotionEffects.getBleedTick() / 20) % 2 == 0) entity.attackEntityFrom(RiftDamage.RIFT_BLEED, (nonPotionEffects.getBleedStrength() + 1F) * 0.5F);
            }
            else {
                nonPotionEffects.reduceBleedTick();
                if ((nonPotionEffects.getBleedTick() / 20) % 2 == 0) entity.attackEntityFrom(RiftDamage.RIFT_BLEED, nonPotionEffects.getBleedStrength() + 1F);
            }
        }
    }
}
