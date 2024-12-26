package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressProvider;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftJournalEditAll implements IMessage {
    private boolean add;

    public RiftJournalEditAll() {}

    public RiftJournalEditAll(boolean add) {
        this.add = add; //add all if true, remove all if false
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.add = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.add);
    }

    public void onClientReceived(Minecraft minecraft, RiftJournalEditAll message, EntityPlayer player, MessageContext messageContext) {
        IPlayerJournalProgress journalProgress = player.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
        if (message.add) {
            for (RiftCreatureType creatureType : RiftCreatureType.values()) {
                if (!journalProgress.getUnlockedCreatures().contains(creatureType)) {
                    journalProgress.unlockCreature(creatureType);
                }
            }
        }
        else journalProgress.resetEntries();
    }


    public static class Handler implements IMessageHandler<RiftJournalEditAll, IMessage> {
        @Override
        public IMessage onMessage(RiftJournalEditAll message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftJournalEditAll message, MessageContext ctx) {
            EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

            IPlayerJournalProgress journalProgress = messagePlayer.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
            if (message.add) {
                for (RiftCreatureType creatureType : RiftCreatureType.values()) {
                    if (!journalProgress.getUnlockedCreatures().contains(creatureType)) {
                        journalProgress.unlockCreature(creatureType);
                    }
                }
            }
            else journalProgress.resetEntries();
        }
    }
}
