package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftRidingAimMessage extends RiftLibMessage<RiftRidingAimMessage> {
    private boolean active;
    private int moveIndex;
    private int targetEntityId;
    private double aimX;
    private double aimY;
    private double aimZ;

    public RiftRidingAimMessage() {}

    public RiftRidingAimMessage(boolean active, int moveIndex, int targetEntityId, double aimX, double aimY, double aimZ) {
        this.active = active;
        this.moveIndex = moveIndex;
        this.targetEntityId = targetEntityId;
        this.aimX = aimX;
        this.aimY = aimY;
        this.aimZ = aimZ;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.active = buffer.readBoolean();
        this.moveIndex = buffer.readInt();
        this.targetEntityId = buffer.readInt();
        this.aimX = buffer.readDouble();
        this.aimY = buffer.readDouble();
        this.aimZ = buffer.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeBoolean(this.active);
        buffer.writeInt(this.moveIndex);
        buffer.writeInt(this.targetEntityId);
        buffer.writeDouble(this.aimX);
        buffer.writeDouble(this.aimY);
        buffer.writeDouble(this.aimZ);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftRidingAimMessage message, EntityPlayer player, MessageContext messageContext) {
        if (!(player.getRidingEntity() instanceof RiftCreature creature) || creature.getControllingPassenger() != player) {
            return;
        }

        creature.setRiddenAimFromRider(
                player, message.active, message.moveIndex, message.targetEntityId,
                message.aimX, message.aimY, message.aimZ
        );
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftRidingAimMessage message, EntityPlayer player, MessageContext messageContext) {}
}
