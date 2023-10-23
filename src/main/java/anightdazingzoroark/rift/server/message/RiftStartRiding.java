package anightdazingzoroark.rift.server.message;

import anightdazingzoroark.rift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftStartRiding extends AbstractMessage<RiftStartRiding> {
    private int creatureId;

    public RiftStartRiding() {}

    public RiftStartRiding(RiftCreature creature) {
        this.creatureId = creature.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(creatureId);
    }

    @Override
    public void onClientReceived(Minecraft client, RiftStartRiding message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer server, RiftStartRiding message, EntityPlayer player, MessageContext messageContext) {
        World world = player.getEntityWorld();
        RiftCreature creature = (RiftCreature)world.getEntityByID(message.creatureId);

        creature.getNavigator().clearPath();
        creature.setAttackTarget(null);
        player.startRiding(creature);
    }
}
