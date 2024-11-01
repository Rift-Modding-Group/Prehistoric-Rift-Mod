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

public class RiftSetCaptured extends AbstractMessage<RiftSetCaptured> {
    private int entityId;
    private boolean isCaptured;

    public RiftSetCaptured() {}

    public RiftSetCaptured(Entity entity, boolean isCaptured) {
        this.entityId = entity.getEntityId();
        this.isCaptured = isCaptured;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.isCaptured = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeBoolean(this.isCaptured);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftSetCaptured message, EntityPlayer player, MessageContext messageContext) {
        INonPotionEffects nonPotionEffects = player.world.getEntityByID(message.entityId).getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        nonPotionEffects.setCaptured(message.isCaptured);
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftSetCaptured message, EntityPlayer player, MessageContext messageContext) {
        INonPotionEffects nonPotionEffects = player.world.getEntityByID(message.entityId).getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        nonPotionEffects.setCaptured(message.isCaptured);
    }
}
