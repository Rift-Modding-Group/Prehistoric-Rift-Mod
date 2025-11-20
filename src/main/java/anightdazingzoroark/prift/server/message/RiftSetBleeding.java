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

public class RiftSetBleeding extends RiftLibMessage<RiftSetBleeding> {
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

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftSetBleeding message, EntityPlayer messagePlayer, MessageContext messageContext) {
        Entity entityToTarget = messagePlayer.world.getEntityByID(message.entityId);
        if (entityToTarget == null) return;
        INonPotionEffects nonPotionEffects = entityToTarget.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        if (nonPotionEffects != null) nonPotionEffects.setBleeding(message.strength, message.ticks);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeOnClient(Minecraft minecraft, RiftSetBleeding message, EntityPlayer messagePlayer, MessageContext messageContext) {
        Entity entityToTarget = messagePlayer.world.getEntityByID(message.entityId);
        if (entityToTarget == null) return;
        INonPotionEffects nonPotionEffects = entityToTarget.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        if (nonPotionEffects != null) nonPotionEffects.setBleeding(message.strength, message.ticks);
    }
}
