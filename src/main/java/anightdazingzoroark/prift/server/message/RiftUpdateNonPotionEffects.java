package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.INonPotionEffects;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsProvider;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RiftUpdateNonPotionEffects extends AbstractMessage<RiftUpdateNonPotionEffects> {
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
    @SideOnly(Side.CLIENT)
    public void onClientReceived(Minecraft minecraft, RiftUpdateNonPotionEffects message, EntityPlayer player, MessageContext messageContext) {
        EntityLivingBase entity = (EntityLivingBase) Minecraft.getMinecraft().world.getEntityByID(message.entityId);
        if (entity != null) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                INonPotionEffects capability = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
                if (capability != null) NonPotionEffectsProvider.readNBT(capability, null, message.nbtTagCompound);
            });
        }
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftUpdateNonPotionEffects message, EntityPlayer player, MessageContext messageContext) {

    }
}
