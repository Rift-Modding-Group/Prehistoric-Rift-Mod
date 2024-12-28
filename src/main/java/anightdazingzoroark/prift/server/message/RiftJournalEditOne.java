package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressProvider;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftJournalEditOne implements IMessage {
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

    public static class Handler implements IMessageHandler<RiftJournalEditOne, IMessage> {
        @Override
        public IMessage onMessage(RiftJournalEditOne message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftJournalEditOne message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;
                IPlayerJournalProgress journalProgress = messagePlayer.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
                if (message.addToEntry) journalProgress.unlockCreature(RiftCreatureType.values()[message.creatureTypeId]);
                else journalProgress.clearCreature(RiftCreatureType.values()[message.creatureTypeId]);
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;
                IPlayerJournalProgress journalProgress = messagePlayer.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
                if (message.addToEntry) journalProgress.unlockCreature(RiftCreatureType.values()[message.creatureTypeId]);
                else journalProgress.clearCreature(RiftCreatureType.values()[message.creatureTypeId]);
            }
        }
    }
}
