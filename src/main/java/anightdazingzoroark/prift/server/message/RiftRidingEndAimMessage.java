package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftRidingEndAimMessage extends RiftLibMessage<RiftRidingEndAimMessage> {
    public RiftRidingEndAimMessage() {}

    @Override
    public void fromBytes(ByteBuf byteBuf) {}

    @Override
    public void toBytes(ByteBuf byteBuf) {}

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftRidingEndAimMessage message, EntityPlayer player, MessageContext messageContext) {
        if (player.getRidingEntity() instanceof RiftCreature creature && creature.getControllingPassenger() == player) {
            creature.stopRiddenAim();
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftRidingEndAimMessage message, EntityPlayer player, MessageContext messageContext) {}
}
