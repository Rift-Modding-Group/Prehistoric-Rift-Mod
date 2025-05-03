package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftUpdatePlayerJournalProgress implements IMessage {
    private NBTTagCompound nbtTagCompound;
    private int playerId;

    public RiftUpdatePlayerJournalProgress() {}

    public RiftUpdatePlayerJournalProgress(NBTBase nbtBase, EntityPlayer player) {
        this.nbtTagCompound = (NBTTagCompound) nbtBase;
        this.playerId = player.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.nbtTagCompound = ByteBufUtils.readTag(buf);
        this.playerId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.nbtTagCompound);
        buf.writeInt(this.playerId);
    }

    public static class Handler implements IMessageHandler<RiftUpdatePlayerJournalProgress, IMessage> {
        @Override
        public IMessage onMessage(RiftUpdatePlayerJournalProgress message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftUpdatePlayerJournalProgress message, MessageContext ctx) {
            EntityPlayer player = (EntityPlayer) Minecraft.getMinecraft().world.getEntityByID(message.playerId);
            if (player != null) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    IPlayerJournalProgress capability = player.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
                    if (capability != null) PlayerJournalProgressProvider.readNBT(capability, null, message.nbtTagCompound);
                });
            }
        }
    }
}
