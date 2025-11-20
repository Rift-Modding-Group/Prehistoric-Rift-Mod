package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressProvider;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RiftUpdatePlayerJournalProgress extends RiftLibMessage<RiftUpdatePlayerJournalProgress> {
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
    public void executeOnServer(MinecraftServer minecraftServer, RiftUpdatePlayerJournalProgress message, EntityPlayer messagePlayer, MessageContext messageContext) {}

    @SideOnly(Side.CLIENT)
    @Override
    public void executeOnClient(Minecraft minecraft, RiftUpdatePlayerJournalProgress message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) minecraft.world.getEntityByID(message.playerId);
        if (player == null) return;
        IPlayerJournalProgress capability = player.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
        if (capability != null) PlayerJournalProgressProvider.readNBT(capability, null, message.nbtTagCompound);
    }
}
