package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressProvider;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.Map;

public class RiftSyncJournal implements IMessage {
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

    public static class Handler implements IMessageHandler<RiftSyncJournal, IMessage> {
        @Override
        public IMessage onMessage(RiftSyncJournal message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftSyncJournal message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerJournalProgress playerJournalProgress = player.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);

                if (playerJournalProgress != null) {
                    RiftMessages.WRAPPER.sendTo(new RiftSyncJournal(player, playerJournalProgress.getEncounteredCreatures()), (EntityPlayerMP) player);
                }
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerJournalProgress playerJournalProgress = player.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);

                if (playerJournalProgress != null) {
                    playerJournalProgress.setEncounteredCreatures(message.encounteredCreatures);
                }
            }
        }
    }
}
