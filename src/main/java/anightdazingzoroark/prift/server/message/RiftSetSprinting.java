package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RiftSetSprinting extends RiftLibMessage<RiftSetSprinting> {
    private int entityId;
    private boolean value;

    public RiftSetSprinting() {}

    public RiftSetSprinting(EntityLivingBase creature, boolean value) {
        this.entityId = creature.getEntityId();
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
    public void executeOnServer(MinecraftServer minecraftServer, RiftSetSprinting message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityLivingBase entityLivingBase = (EntityLivingBase) messagePlayer.world.getEntityByID(message.entityId);
        if (entityLivingBase != null) entityLivingBase.setSprinting(message.value);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeOnClient(Minecraft minecraft, RiftSetSprinting message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityLivingBase entityLivingBase = (EntityLivingBase) messagePlayer.world.getEntityByID(message.entityId);
        if (entityLivingBase != null) entityLivingBase.setSprinting(message.value);
    }
}
