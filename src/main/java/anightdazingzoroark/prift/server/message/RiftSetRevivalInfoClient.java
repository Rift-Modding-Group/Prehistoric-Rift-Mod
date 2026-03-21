package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxHelper;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxProperties;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.HashMap;

public class RiftSetRevivalInfoClient extends RiftLibMessage<RiftSetRevivalInfoClient> {
    private int playerId;
    private HashMap<ImmutablePair<Integer, Integer>, Integer> creatureRevivalMap;

    public RiftSetRevivalInfoClient() {}

    public RiftSetRevivalInfoClient(EntityPlayer player, HashMap<ImmutablePair<Integer, Integer>, Integer> creatureRevivalMap) {
        this.playerId = player.getEntityId();
        this.creatureRevivalMap = creatureRevivalMap;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();

        NBTTagCompound nbtTagCompound = ByteBufUtils.readTag(buf);
        if (nbtTagCompound == null) return;

        this.creatureRevivalMap = PlayerCreatureBoxHelper.parseReviveInfoFromNBTList(
                nbtTagCompound.getTagList("CreatureRevivalMap", 10)
        );
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);

        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        nbtTagCompound.setTag("CreatureRevivalMap", PlayerCreatureBoxHelper.getNBTListFromReviveInfo(this.creatureRevivalMap));
        ByteBufUtils.writeTag(buf, nbtTagCompound);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftSetRevivalInfoClient message, EntityPlayer entityPlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) server.getEntityWorld().getEntityByID(message.playerId);
        if (player == null) return;

        PlayerCreatureBoxProperties creatureBoxProperties = PlayerCreatureBoxHelper.getPlayerCreatureBox(player);
        if (creatureBoxProperties == null) return;

        HashMap<ImmutablePair<Integer, Integer>, Integer> creatureRevivalMap = message.creatureRevivalMap;
        if (creatureRevivalMap == null) return;

        creatureBoxProperties.setCreatureReviveTime(creatureRevivalMap);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftSetRevivalInfoClient message, EntityPlayer entityPlayer, MessageContext messageContext) {

    }
}
