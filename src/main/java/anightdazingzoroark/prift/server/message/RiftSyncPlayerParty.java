package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.CapabilitySyncDirection;
import anightdazingzoroark.prift.server.capabilities.playerParty.IPlayerParty;
import anightdazingzoroark.prift.server.capabilities.playerParty.PlayerPartyHelper;
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

import java.util.Objects;

public class RiftSyncPlayerParty extends RiftLibMessage<RiftSyncPlayerParty> {
    private int playerId;
    private CapabilitySyncDirection syncDirection;
    private NBTTagCompound partyNBTForSync;

    public RiftSyncPlayerParty() {}

    public RiftSyncPlayerParty(EntityPlayer player, CapabilitySyncDirection syncDirection) {
        this.playerId = player.getEntityId();
        this.syncDirection = syncDirection;
        this.partyNBTForSync = new NBTTagCompound();
    }

    public RiftSyncPlayerParty(EntityPlayer player, CapabilitySyncDirection syncDirection, NBTTagCompound partyNBTForSync) {
        this.playerId = player.getEntityId();
        this.syncDirection = syncDirection;
        this.partyNBTForSync = partyNBTForSync;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.syncDirection = CapabilitySyncDirection.values()[buf.readByte()];

        NBTTagCompound nbtTagCompound = ByteBufUtils.readTag(buf);
        this.partyNBTForSync = nbtTagCompound != null ? nbtTagCompound : new NBTTagCompound();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeByte(this.syncDirection.ordinal());
        ByteBufUtils.writeTag(buf, this.partyNBTForSync);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftSyncPlayerParty message, EntityPlayer entityPlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) server.getEntityWorld().getEntityByID(message.playerId);
        IPlayerParty playerParty = PlayerPartyHelper.getPlayerParty(player);
        if (playerParty == null) return;
        if (message.syncDirection == CapabilitySyncDirection.SERVER_TO_CLIENT) {
            NBTTagCompound partyNBTForSync = playerParty.getPartyNBTForSync();
            RiftMessages.WRAPPER.sendTo(new RiftSyncPlayerParty(player, message.syncDirection, partyNBTForSync), (EntityPlayerMP) player);
        }
        else if (message.syncDirection == CapabilitySyncDirection.CLIENT_TO_SERVER) {
            //nbt parsing
            playerParty.parsePartyNBTForSync(message.partyNBTForSync);

            //apply deployment or teleportation
            playerParty.applyDeploymentOrTeleportation(player);
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftSyncPlayerParty message, EntityPlayer entityPlayer, MessageContext messageContext) {
        EntityPlayer player = minecraft.player;
        IPlayerParty playerParty = PlayerPartyHelper.getPlayerParty(player);
        if (playerParty == null) return;
        if (message.syncDirection == CapabilitySyncDirection.SERVER_TO_CLIENT) {
            playerParty.parsePartyNBTForSync(message.partyNBTForSync);
        }
    }
}
