package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.INonPotionEffects;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsProvider;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RiftSetHypnotized extends RiftLibMessage<RiftSetHypnotized> {
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

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftSetHypnotized message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityCreature entity = (EntityCreature) messagePlayer.world.getEntityByID(message.entityId);
        if (entity == null) return;

        RiftCreature hypnotizer = (RiftCreature) messagePlayer.world.getEntityByID(message.hypnotizerId);
        INonPotionEffects nonPotionEffects = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);

        if (nonPotionEffects != null) nonPotionEffects.hypnotize(hypnotizer);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeOnClient(Minecraft minecraft, RiftSetHypnotized message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityCreature entity = (EntityCreature) messagePlayer.world.getEntityByID(message.entityId);
        if (entity == null) return;

        INonPotionEffects nonPotionEffects = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);

        if (nonPotionEffects != null) nonPotionEffects.hypnotize();
    }
}
