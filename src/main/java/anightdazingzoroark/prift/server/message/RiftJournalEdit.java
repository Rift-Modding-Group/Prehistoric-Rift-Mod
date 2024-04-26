package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.PlayerJournalProgress;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftJournalEdit extends AbstractMessage<RiftJournalEdit> {
    private boolean addToEntry;
    private int creatureTypeId;

    public RiftJournalEdit() {}

    public RiftJournalEdit(boolean addToEntry, RiftCreatureType type) {
        this.addToEntry = addToEntry;
        this.creatureTypeId = type.ordinal();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.addToEntry = buf.readBoolean();
        this.creatureTypeId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.addToEntry);
        buf.writeInt(this.creatureTypeId);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftJournalEdit message, EntityPlayer entityPlayer, MessageContext messageContext) {

    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftJournalEdit message, EntityPlayer entityPlayer, MessageContext messageContext) {
        PlayerJournalProgress journalProgress = EntityPropertiesHandler.INSTANCE.getProperties(entityPlayer, PlayerJournalProgress.class);
        if (message.addToEntry) journalProgress.unlockCreature(RiftCreatureType.values()[message.creatureTypeId]);
        else journalProgress.clearCreature(RiftCreatureType.values()[message.creatureTypeId]);
    }
}
