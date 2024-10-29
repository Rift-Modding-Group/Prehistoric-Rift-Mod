package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressProvider;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftJournalEditOne extends AbstractMessage<RiftJournalEditOne> {
    private int creatureTypeId;
    private boolean addToEntry;

    public RiftJournalEditOne() {}

    public RiftJournalEditOne(RiftCreatureType type, boolean addToEntry) {
        this.creatureTypeId = type.ordinal();
        this.addToEntry = addToEntry; //add if true, remove if false
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureTypeId = buf.readInt();
        this.addToEntry = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureTypeId);
        buf.writeBoolean(this.addToEntry);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftJournalEditOne message, EntityPlayer player, MessageContext messageContext) {
        IPlayerJournalProgress journalProgress = player.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
        if (message.addToEntry) journalProgress.unlockCreature(RiftCreatureType.values()[message.creatureTypeId]);
        else journalProgress.clearCreature(RiftCreatureType.values()[message.creatureTypeId]);
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftJournalEditOne message, EntityPlayer player, MessageContext messageContext) {
        IPlayerJournalProgress journalProgress = player.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
        if (message.addToEntry) journalProgress.unlockCreature(RiftCreatureType.values()[message.creatureTypeId]);
        else journalProgress.clearCreature(RiftCreatureType.values()[message.creatureTypeId]);
    }
}
