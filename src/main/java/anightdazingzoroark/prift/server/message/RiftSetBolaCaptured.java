package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.INonPotionEffects;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsProvider;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftSetBolaCaptured extends AbstractMessage<RiftSetBolaCaptured> {
    private int entityId;
    private int ticks;

    public RiftSetBolaCaptured() {}

    public RiftSetBolaCaptured(Entity entity, int ticks) {
        this.entityId = entity.getEntityId();
        this.ticks = ticks;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.ticks = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.ticks);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftSetBolaCaptured message, EntityPlayer player, MessageContext messageContext) {
        INonPotionEffects nonPotionEffects = player.world.getEntityByID(message.entityId).getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        nonPotionEffects.setBolaCaptured(message.ticks);
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftSetBolaCaptured message, EntityPlayer player, MessageContext messageContext) {
        INonPotionEffects nonPotionEffects = player.world.getEntityByID(message.entityId).getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        nonPotionEffects.setBolaCaptured(message.ticks);
    }
}
