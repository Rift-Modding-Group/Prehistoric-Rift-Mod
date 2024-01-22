package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.RiftEntityProperties;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftStartSetWorkstation extends AbstractMessage<RiftStartSetWorkstation> {
    private int creatureId;

    public RiftStartSetWorkstation() {}

    public RiftStartSetWorkstation(RiftCreature creature) {
        this.creatureId = creature.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(creatureId);
    }

    @Override
    public void onClientReceived(Minecraft client, RiftStartSetWorkstation message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer server, RiftStartSetWorkstation message, EntityPlayer player, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature) player.world.getEntityByID(message.creatureId);
        if (creature.getOwner().equals(player)) {
            RiftEntityProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(player, RiftEntityProperties.class);
            properties.settingCreatureWorkstation = true;
            properties.creatureIdForWorkstation = message.creatureId;
            player.sendStatusMessage(new TextComponentTranslation("action.set_creature_workstation_start"), false);
        }
    }
}
