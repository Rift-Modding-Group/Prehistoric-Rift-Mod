package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftClearHomePosFromPopup extends AbstractMessage<RiftClearHomePosFromPopup> {
    private int creatureId;

    public RiftClearHomePosFromPopup() {}

    public RiftClearHomePosFromPopup(RiftCreature creature) {
        this.creatureId = creature.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
    }

    @Override
    public void onClientReceived(Minecraft client, RiftClearHomePosFromPopup message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer server, RiftClearHomePosFromPopup message, EntityPlayer player, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature) player.world.getEntityByID(message.creatureId);
        creature.clearHomePos();
        player.sendStatusMessage(new TextComponentTranslation("tameupdate.clear_home", creature.getName()), false);
    }
}
