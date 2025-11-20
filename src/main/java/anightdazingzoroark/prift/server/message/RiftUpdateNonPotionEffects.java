package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.INonPotionEffects;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsProvider;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftUpdateNonPotionEffects extends RiftLibMessage<RiftUpdateNonPotionEffects> {
    private NBTTagCompound nbtTagCompound;
    private int entityId;

    public RiftUpdateNonPotionEffects() {}

    public RiftUpdateNonPotionEffects(NBTBase nbtBase, EntityLivingBase entity) {
        this.nbtTagCompound = (NBTTagCompound) nbtBase;
        this.entityId = entity.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.nbtTagCompound = ByteBufUtils.readTag(buf);
        this.entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.nbtTagCompound);
        buf.writeInt(this.entityId);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftUpdateNonPotionEffects message, EntityPlayer messagePlayer, MessageContext messageContext) {}

    @Override
    public void executeOnClient(Minecraft minecraft, RiftUpdateNonPotionEffects message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityLivingBase entity = (EntityLivingBase) minecraft.world.getEntityByID(message.entityId);
        if (entity == null) return;
        INonPotionEffects capability = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        if (capability != null) NonPotionEffectsProvider.readNBT(capability, null, message.nbtTagCompound);
    }
}
