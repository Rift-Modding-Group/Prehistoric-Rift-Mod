package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiddenLeapRequest;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.CreatureLeapPlan;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.aiming.RiddenAimTarget;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Requests an aimed leap, then returns the server's accepted arc (or a rejection).
 * */
public class RiftRidingLeapMessage extends RiftLibMessage<RiftRidingLeapMessage> {
    private int creatureId;
    private boolean response;
    @Nullable
    private RiddenAimTarget target;
    @Nullable
    private CreatureLeapPlan plan;

    public RiftRidingLeapMessage() {}

    public RiftRidingLeapMessage(int creatureId, @NotNull RiddenAimTarget target) {
        this.creatureId = creatureId;
        this.target = target;
    }

    public RiftRidingLeapMessage(int creatureId, @Nullable CreatureLeapPlan plan) {
        this.creatureId = creatureId;
        this.response = true;
        this.plan = plan;
    }

    @Override
    public void fromBytes(@NotNull ByteBuf buffer) {
        this.creatureId = buffer.readInt();
        this.response = buffer.readBoolean();
        if (!this.response) {
            int entityId = buffer.readInt();
            Vec3d hit = new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            BlockPos block = null;
            EnumFacing face = null;
            if (buffer.readBoolean()) {
                block = BlockPos.fromLong(buffer.readLong());
                face = EnumFacing.byIndex(buffer.readByte());
            }
            this.target = new RiddenAimTarget(entityId, hit, block, face);
        }
        else if (buffer.readBoolean()) {
            Vec3d start = new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            Vec3d landing = new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            this.plan = new CreatureLeapPlan(start, landing, buffer.readDouble(), buffer.readInt(), buffer.readInt());
        }
    }

    @Override
    public void toBytes(@NotNull ByteBuf buffer) {
        buffer.writeInt(this.creatureId);
        buffer.writeBoolean(this.response);
        if (!this.response && this.target != null) {
            buffer.writeInt(this.target.entityId());
            buffer.writeDouble(this.target.position().x);
            buffer.writeDouble(this.target.position().y);
            buffer.writeDouble(this.target.position().z);
            boolean blockTarget = this.target.blockPosition() != null && this.target.blockFace() != null;
            buffer.writeBoolean(blockTarget);
            if (blockTarget) {
                buffer.writeLong(this.target.blockPosition().toLong());
                buffer.writeByte(this.target.blockFace().getIndex());
            }
        }
        else if (this.response) {
            buffer.writeBoolean(this.plan != null);
            if (this.plan != null) {
                buffer.writeDouble(this.plan.start().x);
                buffer.writeDouble(this.plan.start().y);
                buffer.writeDouble(this.plan.start().z);
                buffer.writeDouble(this.plan.landing().x);
                buffer.writeDouble(this.plan.landing().y);
                buffer.writeDouble(this.plan.landing().z);
                buffer.writeDouble(this.plan.arcHeight());
                buffer.writeInt(this.plan.durationTicks());
                buffer.writeInt(this.plan.horizontalDelayTicks());
            }
        }
    }

    @Override
    public void executeOnServer(
            MinecraftServer server, @NotNull RiftRidingLeapMessage message,
            @NotNull EntityPlayer player, MessageContext context
    ) {
        if (message.response || !(player instanceof EntityPlayerMP serverPlayer)
                || !(player.getRidingEntity() instanceof RiftCreature creature)
                || creature.getEntityId() != message.creatureId || creature.getControllingPassenger() != player
        ) {
            return;
        }
        CreatureLeapPlan accepted = null;
        if (message.target != null) accepted = creature.getCreatureMoveHelper().getLeapHelper().jumpFromRider(player, new RiddenLeapRequest.Aimed(message.target));
        RiftMessages.WRAPPER.sendTo(new RiftRidingLeapMessage(message.creatureId, accepted), serverPlayer);
    }

    @Override
    public void executeOnClient(
            Minecraft minecraft, @NotNull RiftRidingLeapMessage message,
            @NotNull EntityPlayer player, MessageContext context
    ) {
        if (message.response && player.getRidingEntity() instanceof RiftCreature creature
                && creature.getEntityId() == message.creatureId && creature.getControllingPassenger() == player
        ) {
            creature.getCreatureMoveHelper().getLeapHelper().jumpFromRider(player, new RiddenLeapRequest.Approval(message.plan));
        }
    }
}
