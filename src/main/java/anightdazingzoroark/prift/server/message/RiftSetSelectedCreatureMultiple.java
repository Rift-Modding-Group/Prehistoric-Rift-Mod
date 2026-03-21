package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.client.newui.holder.HolderHelper;
import anightdazingzoroark.prift.client.newui.holder.SelectedCreatureInfo;
import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.List;

public class RiftSetSelectedCreatureMultiple extends RiftLibMessage<RiftSetSelectedCreatureMultiple> {
    private int playerId;
    private List<ImmutablePair<SelectedCreatureInfo, CreatureNBT>> positionsToSet;

    public RiftSetSelectedCreatureMultiple() {}

    public RiftSetSelectedCreatureMultiple(EntityPlayer player, List<ImmutablePair<SelectedCreatureInfo, CreatureNBT>> positionsToSet) {
        this.playerId = player.getEntityId();
        this.positionsToSet = positionsToSet;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();

        NBTTagCompound nbtTagCompound = ByteBufUtils.readTag(buf);
        if (nbtTagCompound == null) return;
        NBTTagList positionsToSetNBT = nbtTagCompound.getTagList("PositionsToSet", 10);
        List<ImmutablePair<SelectedCreatureInfo, CreatureNBT>> positionsToSet = new ArrayList<>();
        for (int index = 0; index < positionsToSetNBT.tagCount(); index++) {
            NBTTagCompound nbtInList = positionsToSetNBT.getCompoundTagAt(index);
            if (nbtInList.isEmpty()) continue;

            SelectedCreatureInfo selectedCreatureInfo = SelectedCreatureInfo.createFromNBT(nbtInList.getCompoundTag("SelectedCreatureInfo"));
            if (selectedCreatureInfo == null) continue;

            CreatureNBT creatureNBT = new CreatureNBT(nbtInList.getCompoundTag("CreatureNBTToSet"));

            positionsToSet.add(new ImmutablePair<>(selectedCreatureInfo, creatureNBT));
        }
        this.positionsToSet = positionsToSet;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);

        if (this.positionsToSet == null) return;
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        NBTTagList nbtTagList = new NBTTagList();
        for (ImmutablePair<SelectedCreatureInfo, CreatureNBT> posToSet : this.positionsToSet) {
            NBTTagCompound nbtToAppend = new NBTTagCompound();
            nbtToAppend.setTag("SelectedCreatureInfo", posToSet.getLeft().getNBT());
            nbtToAppend.setTag("CreatureNBTToSet", posToSet.getRight().getCreatureNBT());
            nbtTagList.appendTag(nbtToAppend);
        }
        nbtTagCompound.setTag("PositionsToSet", nbtTagList);
        ByteBufUtils.writeTag(buf, nbtTagCompound);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftSetSelectedCreatureMultiple message, EntityPlayer entityPlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) server.getEntityWorld().getEntityByID(message.playerId);
        if (player == null) return;

        List<ImmutablePair<SelectedCreatureInfo, CreatureNBT>> positionsToSet = message.positionsToSet;
        if (positionsToSet == null) return;

        for (ImmutablePair<SelectedCreatureInfo, CreatureNBT> posToSet : positionsToSet) {
            HolderHelper.setSelectedCreature(player, posToSet.getLeft(), posToSet.getRight());
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftSetSelectedCreatureMultiple message, EntityPlayer entityPlayer, MessageContext messageContext) {

    }
}
