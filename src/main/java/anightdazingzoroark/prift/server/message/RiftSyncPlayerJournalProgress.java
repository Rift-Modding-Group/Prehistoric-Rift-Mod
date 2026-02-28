package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.CapabilitySyncDirection;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.NewPlayerJournalProgressHelper;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftSyncPlayerJournalProgress extends RiftLibMessage<RiftSyncPlayerJournalProgress> {
    private int playerId;
    private CapabilitySyncDirection syncDirection;
    private NBTTagList journalNBTForSync;

    public RiftSyncPlayerJournalProgress() {}

    public RiftSyncPlayerJournalProgress(EntityPlayer player, CapabilitySyncDirection syncDirection) {
        this.playerId = player.getEntityId();
        this.syncDirection = syncDirection;
        this.journalNBTForSync = new NBTTagList();
    }

    public RiftSyncPlayerJournalProgress(EntityPlayer player, CapabilitySyncDirection syncDirection,  NBTTagList journalNBTForSync) {
        this.playerId = player.getEntityId();
        this.syncDirection = syncDirection;
        this.journalNBTForSync = journalNBTForSync;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.syncDirection = CapabilitySyncDirection.values()[buf.readByte()];

        NBTTagCompound nbtTagCompound = ByteBufUtils.readTag(buf);
        this.journalNBTForSync = nbtTagCompound != null ? nbtTagCompound.getTagList("JournalList", 10) : new NBTTagList();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeByte(this.syncDirection.ordinal());

        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        nbtTagCompound.setTag("JournalList", this.journalNBTForSync);
        ByteBufUtils.writeTag(buf, nbtTagCompound);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftSyncPlayerJournalProgress message, EntityPlayer entityPlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) server.getEntityWorld().getEntityByID(message.playerId);
        IPlayerJournalProgress journalProgress = NewPlayerJournalProgressHelper.getPlayerJournalProgress(player);
        if (journalProgress == null) return;
        if (message.syncDirection == CapabilitySyncDirection.SERVER_TO_CLIENT) {
            NBTTagList journalNBTList = journalProgress.getProgressAsNBTList();
            RiftMessages.WRAPPER.sendTo(new RiftSyncPlayerJournalProgress(player, message.syncDirection, journalNBTList), (EntityPlayerMP) player);
        }
        else if (message.syncDirection == CapabilitySyncDirection.CLIENT_TO_SERVER) {
            journalProgress.parseNBTListToProgress(message.journalNBTForSync);
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftSyncPlayerJournalProgress message, EntityPlayer entityPlayer, MessageContext messageContext) {
        EntityPlayer player = minecraft.player;
        IPlayerJournalProgress journalProgress = NewPlayerJournalProgressHelper.getPlayerJournalProgress(player);
        if (journalProgress == null) return;
        if (message.syncDirection == CapabilitySyncDirection.SERVER_TO_CLIENT) {
            journalProgress.parseNBTListToProgress(message.journalNBTForSync);
        }
    }
}
