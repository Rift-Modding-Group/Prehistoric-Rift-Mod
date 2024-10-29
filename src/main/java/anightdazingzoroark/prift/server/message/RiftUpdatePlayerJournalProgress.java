package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressProvider;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RiftUpdatePlayerJournalProgress extends AbstractMessage<RiftUpdatePlayerJournalProgress> {
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

    @Override
    @SideOnly(Side.CLIENT)
    public void onClientReceived(Minecraft minecraft, RiftUpdatePlayerJournalProgress message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) Minecraft.getMinecraft().world.getEntityByID(message.playerId);
        if (player != null) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                IPlayerJournalProgress capability = player.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
                if (capability != null) PlayerJournalProgressProvider.readNBT(capability, null, message.nbtTagCompound);
            });
        }
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftUpdatePlayerJournalProgress message, EntityPlayer messagePlayer, MessageContext messageContext) {}
}
