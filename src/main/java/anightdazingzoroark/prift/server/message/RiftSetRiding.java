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

public class RiftSetRiding extends AbstractMessage<RiftSetRiding> {
    private int entityId;
    private boolean value;

    public RiftSetRiding() {}

    public RiftSetRiding(Entity entity, boolean value) {
        this.entityId = entity.getEntityId();
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.value = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeBoolean(this.value);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftSetRiding message, EntityPlayer player, MessageContext messageContext) {
        INonPotionEffects nonPotionEffects = player.world.getEntityByID(message.entityId).getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        nonPotionEffects.setRiding(message.value);
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftSetRiding message, EntityPlayer player, MessageContext messageContext) {
        INonPotionEffects nonPotionEffects = player.world.getEntityByID(message.entityId).getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        nonPotionEffects.setRiding(message.value);

    }
}
