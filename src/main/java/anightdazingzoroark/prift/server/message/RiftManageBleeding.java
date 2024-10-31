package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.RiftDamage;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.INonPotionEffects;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsProvider;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftManageBleeding extends AbstractMessage<RiftManageBleeding> {
    private int entityId;
    private boolean isMoving;

    public RiftManageBleeding() {}

    public RiftManageBleeding(Entity entity, boolean isMoving) {
        this.entityId = entity.getEntityId();
        this.isMoving = isMoving;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.isMoving = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeBoolean(this.isMoving);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftManageBleeding message, EntityPlayer player, MessageContext messageContext) {

    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftManageBleeding message, EntityPlayer player, MessageContext messageContext) {
        Entity entity = player.world.getEntityByID(message.entityId);
        INonPotionEffects nonPotionEffects = entity.getCapability(NonPotionEffectsProvider.NON_POTION_EFFECTS_CAPABILITY, null);
        if (message.isMoving) {
            nonPotionEffects.reduceBleedTick();
            if ((nonPotionEffects.getBleedTick() / 20) % 2 == 0) entity.attackEntityFrom(RiftDamage.RIFT_BLEED, (nonPotionEffects.getBleedStrength() + 1F) * 0.5F);
        }
        else {
            nonPotionEffects.reduceBleedTick();
            if ((nonPotionEffects.getBleedTick() / 20) % 2 == 0) entity.attackEntityFrom(RiftDamage.RIFT_BLEED, nonPotionEffects.getBleedStrength() + 1F);
        }
    }
}
