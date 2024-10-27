package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IHarvestWhenWandering;
import anightdazingzoroark.prift.server.entity.interfaces.ITurretModeUser;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftSetTurretMode extends AbstractMessage<RiftSetTurretMode> {
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
    public void onClientReceived(Minecraft minecraft, RiftSetTurretMode message, EntityPlayer player, MessageContext messageContext) {

    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftSetTurretMode message, EntityPlayer player, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature)player.world.getEntityByID(message.creatureId);
        ITurretModeUser turretModeUser = (ITurretModeUser) creature;

        //send message
        String messageToSend = message.value ? "action.creature_start_turret_mode" : "action.creature_stop_turret_mode";
        ((EntityPlayer) creature.getOwner()).sendStatusMessage(new TextComponentTranslation(messageToSend), false);

        //set turret mode
        turretModeUser.setTurretMode(message.value);

        //remove or reset speed
        if (message.value) creature.removeSpeed();
        else creature.resetSpeed();
    }
}
