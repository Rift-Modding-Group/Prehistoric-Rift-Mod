package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.INonPotionEffects;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftUpdateNonPotionEffects implements IMessage {
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

    public static class Handler implements IMessageHandler<RiftUpdateNonPotionEffects, IMessage> {
        @Override
        public IMessage onMessage(RiftUpdateNonPotionEffects message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftUpdateNonPotionEffects message, MessageContext ctx) {
            EntityLivingBase entity = (EntityLivingBase) Minecraft.getMinecraft().world.getEntityByID(message.entityId);
            if (entity != null) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    INonPotionEffects capability = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
                    if (capability != null) NonPotionEffectsProvider.readNBT(capability, null, message.nbtTagCompound);
                });
            }
        }
    }
}
