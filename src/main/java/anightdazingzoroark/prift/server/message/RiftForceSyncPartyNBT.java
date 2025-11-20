package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class RiftForceSyncPartyNBT extends RiftLibMessage<RiftForceSyncPartyNBT> {
    private int playerId;
    private int listSize;
    private FixedSizeList<CreatureNBT> tagCompounds;

    public RiftForceSyncPartyNBT() {}

    public RiftForceSyncPartyNBT(EntityPlayer player) {
        this(player, new FixedSizeList<>(0));
    }

    public RiftForceSyncPartyNBT(EntityPlayer player, FixedSizeList<CreatureNBT> tagCompounds) {
        this.playerId = player.getEntityId();
        this.listSize = tagCompounds.size();
        this.tagCompounds = tagCompounds;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.listSize = buf.readInt();

        NBTTagCompound compound = ByteBufUtils.readTag(buf);
        this.tagCompounds = this.setNBTTagListToNBTList(compound.getTagList("List", 10), this.listSize);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.listSize);

        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("List", this.setNBTListToNBTTagList(this.tagCompounds));
        ByteBufUtils.writeTag(buf, compound);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftForceSyncPartyNBT message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        if (player == null) return;
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        if (playerTamedCreatures == null) return;
        RiftMessages.WRAPPER.sendTo(new RiftForceSyncPartyNBT(player, playerTamedCreatures.getPartyNBT()), (EntityPlayerMP) player);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeOnClient(Minecraft minecraft, RiftForceSyncPartyNBT message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        if (player == null) return;
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        if (playerTamedCreatures == null) return;
        playerTamedCreatures.setPartyNBT(message.tagCompounds);
    }

    private NBTTagList setNBTListToNBTTagList(FixedSizeList<CreatureNBT> tagCompounds) {
        NBTTagList tagList = new NBTTagList();
        for (CreatureNBT tagCompound : tagCompounds.getList()) tagList.appendTag(tagCompound.getCreatureNBT());
        return tagList;
    }

    private FixedSizeList<CreatureNBT> setNBTTagListToNBTList(NBTTagList tagList, int size) {
        FixedSizeList<CreatureNBT> compoundList = new FixedSizeList<>(size, new CreatureNBT());
        for (int x = 0; x < tagList.tagCount(); x++) {
            CreatureNBT tagCompound = new CreatureNBT((NBTTagCompound) tagList.get(x));
            compoundList.set(x, tagCompound);
        }
        return compoundList;
    }
}
