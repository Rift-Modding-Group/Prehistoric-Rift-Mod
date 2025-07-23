package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressProvider;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftJournalEditOne implements IMessage {
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

    public static class Handler implements IMessageHandler<RiftJournalEditOne, IMessage> {
        @Override
        public IMessage onMessage(RiftJournalEditOne message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftJournalEditOne message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerJournalProgress journalProgress = player.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
                if (message.addToEntry) {
                    if (message.unlock) journalProgress.unlockCreature(RiftCreatureType.values()[message.creatureTypeId]);
                    else journalProgress.discoverCreature(RiftCreatureType.values()[message.creatureTypeId]);
                }
                else journalProgress.clearCreature(RiftCreatureType.values()[message.creatureTypeId]);
            }
        }
    }
}
