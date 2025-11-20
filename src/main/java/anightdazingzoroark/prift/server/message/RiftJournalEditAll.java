package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressProvider;
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

public class RiftJournalEditAll extends RiftLibMessage<RiftJournalEditAll> {
    private int playerId;
    private boolean add;

    public RiftJournalEditAll() {}

    public RiftJournalEditAll(EntityPlayer player, boolean add) {
        this.playerId = player.getEntityId();
        this.add = add; //add all if true, remove all if false
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.add = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeBoolean(this.add);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftJournalEditAll message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        if (player == null) return;
        IPlayerJournalProgress journalProgress = player.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
        if (journalProgress == null) return;
        if (message.add) journalProgress.unlockAllEntries();
        else journalProgress.resetEntries();
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftJournalEditAll message, EntityPlayer messagePlayer, MessageContext messageContext) {}
}
