package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftSetTurretMode extends RiftLibMessage<RiftSetTurretMode> {
    private int creatureId;
    private boolean value;

    public RiftSetTurretMode() {}

    public RiftSetTurretMode(RiftCreature creature, boolean value) {
        this.creatureId = creature.getEntityId();
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.value = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeBoolean(this.value);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftSetTurretMode message, EntityPlayer messagePlayer, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature) messagePlayer.world.getEntityByID(message.creatureId);

        //send message
        String messageToSend = message.value ? "action.creature_start_turret_mode" : "action.creature_stop_turret_mode";
        ((EntityPlayer) creature.getOwner()).sendStatusMessage(new TextComponentTranslation(messageToSend), false);

        //set turret mode
        creature.setTurretMode(message.value);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftSetTurretMode riftSetTurretMode, EntityPlayer messagePlayer, MessageContext messageContext) {}
}
