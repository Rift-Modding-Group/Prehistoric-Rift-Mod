package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.INonPotionEffects;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsProvider;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RiftSetEntityGrabbed extends RiftLibMessage<RiftSetEntityGrabbed> {
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

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftSetEntityGrabbed message, EntityPlayer messagePlayer, MessageContext messageContext) {
        Entity grabbedEntity = messagePlayer.world.getEntityByID(message.entityId);
        if (grabbedEntity == null) return;
        INonPotionEffects nonPotionEffects = grabbedEntity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        if (nonPotionEffects != null) nonPotionEffects.setGrabbed(message.isGrabbed);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeOnClient(Minecraft minecraft, RiftSetEntityGrabbed message, EntityPlayer messagePlayer, MessageContext messageContext) {
        Entity grabbedEntity = messagePlayer.world.getEntityByID(message.entityId);
        if (grabbedEntity == null) return;
        INonPotionEffects nonPotionEffects = grabbedEntity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        if (nonPotionEffects != null) nonPotionEffects.setGrabbed(message.isGrabbed);
    }
}
