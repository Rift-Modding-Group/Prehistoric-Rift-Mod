package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftStartRiding extends RiftLibMessage<RiftStartRiding> {
    private int entityId;

    public RiftStartRiding() {}

    public RiftStartRiding(EntityLiving entity) {
        this.entityId = entity.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftStartRiding message, EntityPlayer messagePlayer, MessageContext messageContext) {
        World world = messagePlayer.getEntityWorld();
        EntityLiving entity = (EntityLiving)world.getEntityByID(message.entityId);

        if (entity != null) {
            entity.getNavigator().clearPath();
            entity.setAttackTarget(null);
            messagePlayer.startRiding(entity, true);
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftStartRiding message, EntityPlayer messagePlayer, MessageContext messageContext) {}
}
