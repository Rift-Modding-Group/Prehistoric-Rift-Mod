package anightdazingzoroark.rift.server.message;

import anightdazingzoroark.rift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftChangeCreatureName extends AbstractMessage<RiftChangeCreatureName> {
    private int creatureId;
    private String newName;

    public RiftChangeCreatureName() {}

    public RiftChangeCreatureName(RiftCreature creature, String newName) {
        this.creatureId = creature.getEntityId();
        this.newName = newName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();

        int stringLength = buf.readInt();
        byte[] stringBytes = new byte[stringLength];
        buf.readBytes(stringBytes);
        this.newName = new String(stringBytes);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);

        byte[] stringBytes = this.newName.getBytes();
        buf.writeInt(stringBytes.length);
        buf.writeBytes(stringBytes);
    }

    @Override
    public void onClientReceived(Minecraft client, RiftChangeCreatureName message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer server, RiftChangeCreatureName message, EntityPlayer player, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature) player.world.getEntityByID(message.creatureId);
        creature.setCustomNameTag(message.newName);
    }
}
