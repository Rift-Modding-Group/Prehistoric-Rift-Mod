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

public class RiftStopBleeding extends RiftLibMessage<RiftStopBleeding> {
    private int entityId;

    public RiftStopBleeding() {}

    public RiftStopBleeding(Entity entity) {
        this.entityId = entity.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftStopBleeding message, EntityPlayer messagePlayer, MessageContext messageContext) {
        Entity entityToTarget = messagePlayer.world.getEntityByID(message.entityId);
        if (entityToTarget == null) return;
        INonPotionEffects nonPotionEffects = entityToTarget.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        if (nonPotionEffects != null) nonPotionEffects.stopBleeding();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeOnClient(Minecraft minecraft, RiftStopBleeding message, EntityPlayer messagePlayer, MessageContext messageContext) {
        Entity entityToTarget = messagePlayer.world.getEntityByID(message.entityId);
        if (entityToTarget == null) return;
        INonPotionEffects nonPotionEffects = entityToTarget.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        if (nonPotionEffects != null) nonPotionEffects.stopBleeding();
    }
}
