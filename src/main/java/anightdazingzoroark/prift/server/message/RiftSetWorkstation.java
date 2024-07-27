package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.RiftEntityProperties;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftSetWorkstation extends AbstractMessage<RiftSetWorkstation> {
    private int creatureId;
    private boolean startUse;

    public RiftSetWorkstation() {}

    public RiftSetWorkstation(RiftCreature creature, boolean startUse) {
        this.creatureId = creature.getEntityId();
        this.startUse = startUse;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.startUse = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeBoolean(this.startUse);
    }

    @Override
    public void onClientReceived(Minecraft client, RiftSetWorkstation message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer server, RiftSetWorkstation message, EntityPlayer player, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature) player.world.getEntityByID(message.creatureId);
        if (creature.getOwner().equals(player)) {
            if (message.startUse) {
                creature.setTameStatus(TameStatusType.STAND);
                RiftEntityProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(player, RiftEntityProperties.class);
                properties.settingCreatureWorkstation = true;
                properties.creatureIdForWorkstation = message.creatureId;
                player.sendStatusMessage(new TextComponentTranslation("action.set_creature_workstation_start"), false);
            }
            else ((IWorkstationUser)creature).clearWorkstation(false);
        }
    }
}
