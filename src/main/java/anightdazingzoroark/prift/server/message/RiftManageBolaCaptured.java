package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.INonPotionEffects;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsProvider;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftManageBolaCaptured extends AbstractMessage<RiftManageBolaCaptured> {
    private int entityId;

    public RiftManageBolaCaptured() {}

    public RiftManageBolaCaptured(Entity entity) {
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
    public void onClientReceived(Minecraft minecraft, RiftManageBolaCaptured message, EntityPlayer player, MessageContext messageContext) {

    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftManageBolaCaptured message, EntityPlayer player, MessageContext messageContext) {
        Entity entity = player.world.getEntityByID(message.entityId);
        INonPotionEffects nonPotionEffects = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        nonPotionEffects.reduceBolaCapturedTick();
        if (entity instanceof EntityLivingBase) ((EntityLivingBase)entity).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 255));

    }
}
