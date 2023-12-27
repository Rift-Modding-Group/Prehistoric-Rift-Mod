package anightdazingzoroark.prift.server.message;

import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftStartRiding extends AbstractMessage<RiftStartRiding> {
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
    public void onClientReceived(Minecraft client, RiftStartRiding message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer server, RiftStartRiding message, EntityPlayer player, MessageContext messageContext) {
        World world = player.getEntityWorld();
        EntityLiving entity = (EntityLiving)world.getEntityByID(message.entityId);

        entity.getNavigator().clearPath();
        entity.setAttackTarget(null);
        player.startRiding(entity);
    }
}
