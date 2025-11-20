package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.INonPotionEffects;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsProvider;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RiftSetRiding extends RiftLibMessage<RiftSetRiding> {
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
    public void executeOnServer(MinecraftServer minecraftServer, RiftSetRiding message, EntityPlayer messagePlayer, MessageContext messageContext) {
        Entity entityToTarget = messagePlayer.world.getEntityByID(message.entityId);
        if (entityToTarget == null) return;
        INonPotionEffects nonPotionEffects = messagePlayer.world.getEntityByID(message.entityId).getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        if (nonPotionEffects != null) nonPotionEffects.setRiding(message.value);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeOnClient(Minecraft minecraft, RiftSetRiding message, EntityPlayer messagePlayer, MessageContext messageContext) {
        Entity entityToTarget = messagePlayer.world.getEntityByID(message.entityId);
        if (entityToTarget == null) return;
        INonPotionEffects nonPotionEffects = messagePlayer.world.getEntityByID(message.entityId).getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        if (nonPotionEffects != null) nonPotionEffects.setRiding(message.value);
    }
}
