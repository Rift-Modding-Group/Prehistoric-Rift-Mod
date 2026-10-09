package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.Nullable;

public class RiftRidingAimMessage extends RiftLibMessage<RiftRidingAimMessage> {
    private boolean active;
    private int moveIndex;
    private int targetEntityId;
    private double aimX;
    private double aimY;
    private double aimZ;
    private boolean hasTargetBlock;
    private int targetBlockFace;
    private int targetBlockX;
    private int targetBlockY;
    private int targetBlockZ;

    public RiftRidingAimMessage() {}

    public RiftRidingAimMessage(
            boolean active, int moveIndex, int targetEntityId,
            double aimX, double aimY, double aimZ,
            @Nullable BlockPos targetBlock, @Nullable EnumFacing targetBlockFace
    ) {
        this.active = active;
        this.moveIndex = moveIndex;
        this.targetEntityId = targetEntityId;
        this.aimX = aimX;
        this.aimY = aimY;
        this.aimZ = aimZ;
        this.hasTargetBlock = targetBlock != null && targetBlockFace != null;
        if (this.hasTargetBlock) {
            this.targetBlockX = targetBlock.getX();
            this.targetBlockY = targetBlock.getY();
            this.targetBlockZ = targetBlock.getZ();
            this.targetBlockFace = targetBlockFace.getIndex();
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.active = buffer.readBoolean();
        this.moveIndex = buffer.readInt();
        this.targetEntityId = buffer.readInt();
        this.aimX = buffer.readDouble();
        this.aimY = buffer.readDouble();
        this.aimZ = buffer.readDouble();
        this.hasTargetBlock = buffer.readBoolean();
        if (this.hasTargetBlock) {
            this.targetBlockX = buffer.readInt();
            this.targetBlockY = buffer.readInt();
            this.targetBlockZ = buffer.readInt();
            this.targetBlockFace = buffer.readByte();
        }
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeBoolean(this.active);
        buffer.writeInt(this.moveIndex);
        buffer.writeInt(this.targetEntityId);
        buffer.writeDouble(this.aimX);
        buffer.writeDouble(this.aimY);
        buffer.writeDouble(this.aimZ);
        buffer.writeBoolean(this.hasTargetBlock);
        if (this.hasTargetBlock) {
            buffer.writeInt(this.targetBlockX);
            buffer.writeInt(this.targetBlockY);
            buffer.writeInt(this.targetBlockZ);
            buffer.writeByte(this.targetBlockFace);
        }
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftRidingAimMessage message, EntityPlayer player, MessageContext messageContext) {
        if (!(player.getRidingEntity() instanceof RiftCreature creature) || creature.getControllingPassenger() != player) {
            return;
        }

        BlockPos targetBlock = message.hasTargetBlock ? new BlockPos(message.targetBlockX, message.targetBlockY, message.targetBlockZ) : null;
        EnumFacing targetBlockFace = message.hasTargetBlock ? EnumFacing.byIndex(message.targetBlockFace) : null;
        creature.setRiddenAimFromRider(
                player, message.active, message.moveIndex, message.targetEntityId,
                message.aimX, message.aimY, message.aimZ, targetBlock, targetBlockFace
        );
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftRidingAimMessage message, EntityPlayer player, MessageContext messageContext) {}
}
