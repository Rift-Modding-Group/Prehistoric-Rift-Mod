package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressProvider;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftJournalEditOne extends RiftLibMessage<RiftJournalEditOne> {
    private int playerId;
    private int creatureTypeId;
    private boolean addToEntry;
    private boolean unlock;

    public RiftJournalEditOne() {}

    public RiftJournalEditOne(EntityPlayer player, RiftCreatureType type, boolean addToEntry) {
        this(player, type, addToEntry, true);
    }

    public RiftJournalEditOne(EntityPlayer player, RiftCreatureType type, boolean addToEntry, boolean unlock) {
        this.playerId = player.getEntityId();
        this.creatureTypeId = type.ordinal();
        this.addToEntry = addToEntry; //add if true, remove if false
        this.unlock = unlock; //unlock if true, discover if false, ignore when addToEntry is false
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.creatureTypeId = buf.readInt();
        this.addToEntry = buf.readBoolean();
        this.unlock = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.creatureTypeId);
        buf.writeBoolean(this.addToEntry);
        buf.writeBoolean(this.unlock);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftJournalEditOne message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        if (player == null) return;
        IPlayerJournalProgress journalProgress = player.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
        if (journalProgress == null) return;
        if (message.addToEntry) {
            if (message.unlock) journalProgress.unlockCreature(RiftCreatureType.values()[message.creatureTypeId]);
            else journalProgress.discoverCreature(RiftCreatureType.values()[message.creatureTypeId]);
        }
        else journalProgress.clearCreature(RiftCreatureType.values()[message.creatureTypeId]);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftJournalEditOne message, EntityPlayer messagePlayer, MessageContext messageContext) {}
}
