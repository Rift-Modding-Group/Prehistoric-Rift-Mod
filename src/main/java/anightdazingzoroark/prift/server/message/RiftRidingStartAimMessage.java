package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.aiming.RiddenAimArea;
import anightdazingzoroark.prift.server.entity.creatureMoves.aiming.RiddenAimTarget;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.Nullable;

public class RiftRidingStartAimMessage extends RiftLibMessage<RiftRidingStartAimMessage> {
    private int moveIndex;
    // Empty-space aim has no entity or surface target; it uses the same launch physics.
    private boolean targetlessRanged;
    @Nullable
    private RiddenAimTarget aimTarget;
    @Nullable
    private RiddenAimArea aimArea;

    public RiftRidingStartAimMessage() {}

    public RiftRidingStartAimMessage(int moveIndex, boolean targetlessRanged, @Nullable RiddenAimTarget aimTarget) {
        this(moveIndex, targetlessRanged, aimTarget, null);
    }

    public RiftRidingStartAimMessage(
            int moveIndex, boolean targetlessRanged,
            @Nullable RiddenAimTarget aimTarget, @Nullable RiddenAimArea aimArea
    ) {
        this.moveIndex = moveIndex;
        this.targetlessRanged = targetlessRanged;
        this.aimTarget = aimTarget;
        this.aimArea = aimArea;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.moveIndex = buffer.readInt();
        this.targetlessRanged = buffer.readBoolean();
        int targetEntityId = buffer.readInt();
        Vec3d aimPosition = new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        BlockPos targetBlock = null;
        EnumFacing targetBlockFace = null;
        if (buffer.readBoolean()) {
            targetBlock = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
            targetBlockFace = EnumFacing.byIndex(buffer.readByte());
        }
        this.aimTarget = new RiddenAimTarget(targetEntityId, aimPosition, targetBlock, targetBlockFace);
        this.aimArea = null;
        if (buffer.readBoolean()) {
            this.aimArea = new RiddenAimArea(
                    new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                    new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                    buffer.readDouble(), buffer.readDouble()
            );
        }
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(this.moveIndex);
        buffer.writeBoolean(this.targetlessRanged);
        Vec3d aimPosition = this.aimTarget == null ? Vec3d.ZERO : this.aimTarget.position();
        BlockPos targetBlock = this.aimTarget == null ? null : this.aimTarget.blockPosition();
        EnumFacing targetBlockFace = this.aimTarget == null ? null : this.aimTarget.blockFace();
        buffer.writeInt(this.aimTarget == null ? -1 : this.aimTarget.entityId());
        buffer.writeDouble(aimPosition.x);
        buffer.writeDouble(aimPosition.y);
        buffer.writeDouble(aimPosition.z);
        boolean hasTargetBlock = targetBlock != null && targetBlockFace != null;
        buffer.writeBoolean(hasTargetBlock);
        if (hasTargetBlock) {
            buffer.writeInt(targetBlock.getX());
            buffer.writeInt(targetBlock.getY());
            buffer.writeInt(targetBlock.getZ());
            buffer.writeByte(targetBlockFace.getIndex());
        }
        buffer.writeBoolean(this.aimArea != null);
        if (this.aimArea != null) {
            buffer.writeDouble(this.aimArea.origin().x);
            buffer.writeDouble(this.aimArea.origin().y);
            buffer.writeDouble(this.aimArea.origin().z);
            buffer.writeDouble(this.aimArea.forward().x);
            buffer.writeDouble(this.aimArea.forward().y);
            buffer.writeDouble(this.aimArea.forward().z);
            buffer.writeDouble(this.aimArea.horizontalSlope());
            buffer.writeDouble(this.aimArea.verticalSlope());
        }
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftRidingStartAimMessage message, EntityPlayer player, MessageContext messageContext) {
        if (!(player.getRidingEntity() instanceof RiftCreature creature) || creature.getControllingPassenger() != player) {
            return;
        }

        creature.setRiddenAimFromRider(player, message.moveIndex, message.targetlessRanged, message.aimTarget, message.aimArea);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftRidingStartAimMessage message, EntityPlayer player, MessageContext messageContext) {}
}
