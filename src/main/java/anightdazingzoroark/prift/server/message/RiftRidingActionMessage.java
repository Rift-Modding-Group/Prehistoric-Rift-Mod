package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftRidingActionMessage extends RiftLibMessage<RiftRidingActionMessage> {
    private byte action;
    private int value;
    private boolean active;

    public RiftRidingActionMessage() {}

    public RiftRidingActionMessage(Action action, int value, boolean active) {
        this.action = (byte) action.ordinal();
        this.value = value;
        this.active = active;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.action = buffer.readByte();
        this.value = buffer.readInt();
        this.active = buffer.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeByte(this.action);
        buffer.writeInt(this.value);
        buffer.writeBoolean(this.active);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftRidingActionMessage message, EntityPlayer player, MessageContext messageContext) {
        if (message.action < 0 || message.action >= Action.values().length) return;
        if (!(player.getRidingEntity() instanceof RiftCreature creature) || creature.getControllingPassenger() != player) {
            return;
        }

        switch (Action.values()[message.action]) {
            case START_MOVE -> creature.useMoveFromRider(player, message.value);
            case RELEASE_MOVE -> creature.releaseMoveFromRider(player);
            case JUMP -> creature.jumpFromRider(player, message.value, message.active);
            case SET_SPRINTING -> creature.setSprintingFromRider(player, message.active);
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftRidingActionMessage message, EntityPlayer player, MessageContext messageContext) {}

    public enum Action {
        START_MOVE,
        RELEASE_MOVE,
        JUMP,
        SET_SPRINTING
    }
}
