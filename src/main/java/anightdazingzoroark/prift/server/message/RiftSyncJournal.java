package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressProvider;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class RiftSyncJournal extends RiftLibMessage<RiftSyncJournal> {
    private int playerId;
    private Map<RiftCreatureType, Boolean> encounteredCreatures;

    public RiftSyncJournal() {}

    public RiftSyncJournal(EntityPlayer player) {
        this(player, new HashMap<>());
    }

    public RiftSyncJournal(EntityPlayer player, Map<RiftCreatureType, Boolean> encounteredCreatures) {
        this.playerId = player.getEntityId();
        this.encounteredCreatures = encounteredCreatures;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        int size = buf.readInt();
        this.encounteredCreatures = new HashMap<>();
        for (int i = 0; i < size; i++) {
            RiftCreatureType creatureType = RiftCreatureType.values()[buf.readInt()];
            boolean value = buf.readBoolean();
            this.encounteredCreatures.put(creatureType, value);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.encounteredCreatures.size());
        for (Map.Entry<RiftCreatureType, Boolean> entry : this.encounteredCreatures.entrySet()) {
            buf.writeInt(entry.getKey().ordinal());
            buf.writeBoolean(entry.getValue());
        }
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftSyncJournal message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        if (player == null) return;
        IPlayerJournalProgress playerJournalProgress = player.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);

        if (playerJournalProgress != null) {
            RiftMessages.WRAPPER.sendTo(new RiftSyncJournal(player, playerJournalProgress.getEncounteredCreatures()), (EntityPlayerMP) player);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeOnClient(Minecraft minecraft, RiftSyncJournal message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        if (player == null) return;
        IPlayerJournalProgress playerJournalProgress = player.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);

        if (playerJournalProgress != null) playerJournalProgress.setEncounteredCreatures(message.encounteredCreatures);
    }
}
