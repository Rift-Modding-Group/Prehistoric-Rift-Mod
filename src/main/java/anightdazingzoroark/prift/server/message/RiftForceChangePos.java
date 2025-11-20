package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftForceChangePos extends RiftLibMessage<RiftForceChangePos> {
    private int creatureId;
    private double x;
    private double y;
    private double z;

    public RiftForceChangePos() {}

    public RiftForceChangePos(RiftCreature creature, double x, double y, double z) {
        this.creatureId = creature.getEntityId();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftForceChangePos message, EntityPlayer messagePlayer, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature) messagePlayer.world.getEntityByID(message.creatureId);
        if (creature != null) creature.setPosition(message.x, message.y, message.z);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftForceChangePos riftForceChangePos, EntityPlayer entityPlayer, MessageContext messageContext) {}
}
