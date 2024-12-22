package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class RiftForceSyncBoxNBT extends AbstractMessage<RiftForceSyncBoxNBT> {
    private int playerId;
    private List<NBTTagCompound> tagCompounds;

    public RiftForceSyncBoxNBT() {}

    public RiftForceSyncBoxNBT(EntityPlayer player) {
        this(player, new ArrayList<>());
    }

    public RiftForceSyncBoxNBT(EntityPlayer player, List<NBTTagCompound> tagCompounds) {
        this.playerId = player.getEntityId();
        this.tagCompounds = tagCompounds;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();

        NBTTagCompound compound = ByteBufUtils.readTag(buf);
        this.tagCompounds = this.setNBTTagListToNBTList(compound.getTagList("List", 10));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);

        NBTTagCompound compound = new NBTTagCompound();
        if (this.tagCompounds.isEmpty()) compound.setTag("List", new NBTTagList());
        else compound.setTag("List", this.setNBTListToNBTTagList(this.tagCompounds));
        ByteBufUtils.writeTag(buf, compound);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftForceSyncBoxNBT message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        if (message.tagCompounds.isEmpty()) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncBoxNBT(player, playerTamedCreatures.getBoxNBT()));
        else playerTamedCreatures.setBoxNBT(message.tagCompounds);
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftForceSyncBoxNBT message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        if (message.tagCompounds.isEmpty()) RiftMessages.WRAPPER.sendToAll(new RiftForceSyncBoxNBT(player, playerTamedCreatures.getBoxNBT()));
        else playerTamedCreatures.setBoxNBT(message.tagCompounds);
    }

    private NBTTagList setNBTListToNBTTagList(List<NBTTagCompound> tagCompounds) {
        NBTTagList tagList = new NBTTagList();
        for (NBTTagCompound tagCompound : tagCompounds) tagList.appendTag(tagCompound);
        return tagList;
    }

    private List<NBTTagCompound> setNBTTagListToNBTList(NBTTagList tagList) {
        List<NBTTagCompound> compoundList = new ArrayList<>();
        for (NBTBase nbtBase : tagList) compoundList.add((NBTTagCompound) nbtBase);
        return compoundList;
    }
}
