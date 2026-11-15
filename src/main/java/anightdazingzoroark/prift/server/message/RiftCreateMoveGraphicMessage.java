package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.api.creature.CreatureMoveGraphic;
import anightdazingzoroark.prift.client.rendering.CreatureMoveGraphicRenderer;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

public class RiftCreateMoveGraphicMessage extends RiftLibMessage<RiftCreateMoveGraphicMessage> {
    private int dimension;
    private CreatureMoveGraphic.Playback playback;

    public RiftCreateMoveGraphicMessage() {}

    public RiftCreateMoveGraphicMessage(int dimension, @NotNull CreatureMoveGraphic.Playback playback) {
        this.dimension = dimension;
        this.playback = playback;
    }

    @Override
    public void fromBytes(@NotNull ByteBuf buffer) {
        this.dimension = buffer.readInt();
        this.playback = new CreatureMoveGraphic.Playback(
                new CreatureMoveGraphic(ByteBufUtils.readUTF8String(buffer), ByteBufUtils.readUTF8String(buffer),
                        buffer.readBoolean() ? ByteBufUtils.readUTF8String(buffer) : null),
                new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                buffer.readLong(), buffer.readInt(), buffer.readLong()
        );
    }

    @Override
    public void toBytes(@NotNull ByteBuf buffer) {
        buffer.writeInt(this.dimension);
        ByteBufUtils.writeUTF8String(buffer, this.playback.graphic().name());
        ByteBufUtils.writeUTF8String(buffer, this.playback.graphic().animation());
        buffer.writeBoolean(this.playback.graphic().locator() != null);
        if (this.playback.graphic().locator() != null) ByteBufUtils.writeUTF8String(buffer, this.playback.graphic().locator());
        buffer.writeDouble(this.playback.origin().x);
        buffer.writeDouble(this.playback.origin().y);
        buffer.writeDouble(this.playback.origin().z);
        buffer.writeDouble(this.playback.destination().x);
        buffer.writeDouble(this.playback.destination().y);
        buffer.writeDouble(this.playback.destination().z);
        buffer.writeLong(this.playback.startTime());
        buffer.writeInt(this.playback.sourceEntityId());
        buffer.writeLong(this.playback.moveUseId());
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftCreateMoveGraphicMessage message, EntityPlayer player, MessageContext context) {}

    @Override
    public void executeOnClient(Minecraft minecraft, RiftCreateMoveGraphicMessage message, EntityPlayer player, MessageContext context) {
        if (minecraft.world == null || minecraft.world.provider.getDimension() != message.dimension) return;
        CreatureMoveGraphic.Playback playback = message.playback;
        if (playback != null) CreatureMoveGraphicRenderer.addGraphic(minecraft.world, playback);
    }
}
