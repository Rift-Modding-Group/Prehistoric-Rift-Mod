package anightdazingzoroark.prift.server.properties.playerCreatureBox;

import anightdazingzoroark.prift.propertySystem.Property;
import anightdazingzoroark.prift.server.message.RiftChangeCreatureBoxName;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftSetBoxLastOpenedTime;
import anightdazingzoroark.prift.server.message.RiftSetRevivalInfoClient;
import anightdazingzoroark.prift.server.properties.RiftPropertyRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerCreatureBoxHelper {
    public static PlayerCreatureBoxProperties getPlayerCreatureBox(EntityPlayer player) {
        if (player == null) return null;
        return Property.getProperty(RiftPropertyRegistry.PLAYER_CREATURE_BOX, player);
    }

    public static void changeBoxNameClient(EntityPlayer player, int index, String newBoxName) {
        if (player == null || !player.world.isRemote) return;
        RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureBoxName(player, index, newBoxName));
    }

    public static void setLastOpenedTimeClient(EntityPlayer player, int lastOpenedTime) {
        if (player == null || !player.world.isRemote) return;
        RiftMessages.WRAPPER.sendToServer(new RiftSetBoxLastOpenedTime(player, lastOpenedTime));
    }

    public static void setRevivalClient(EntityPlayer player, HashMap<ImmutablePair<Integer, Integer>, Integer> creatureRevivalMap) {
        if (player == null || !player.world.isRemote) return;
        RiftMessages.WRAPPER.sendToServer(new RiftSetRevivalInfoClient(player, creatureRevivalMap));
    }

    //the return value is for positions in the box, represented as a pair, that have been revived
    public static List<ImmutablePair<Integer, Integer>> manageRevival(
            HashMap<ImmutablePair<Integer, Integer>, Integer> revivalHashMap
    ) {
        List<ImmutablePair<Integer, Integer>> toReturn = new ArrayList<>();

        for (Map.Entry<ImmutablePair<Integer, Integer>, Integer> entry : revivalHashMap.entrySet()) {
            entry.setValue(entry.getValue() - 1);
            if (entry.getValue() <= 0) toReturn.add(entry.getKey());
        }

        return toReturn;
    }

    public static NBTTagList getNBTListFromReviveInfo(HashMap<ImmutablePair<Integer, Integer>, Integer> creatureRevivalMap) {
        NBTTagList toReturn = new NBTTagList();
        if (creatureRevivalMap == null) return toReturn;
        for (Map.Entry<ImmutablePair<Integer, Integer>, Integer> entry : creatureRevivalMap.entrySet()) {
            NBTTagCompound nbtToAppend = new NBTTagCompound();
            nbtToAppend.setInteger("BoxIndex", entry.getKey().getLeft());
            nbtToAppend.setInteger("Index", entry.getKey().getRight());
            nbtToAppend.setInteger("ReviveTime", entry.getValue());
            toReturn.appendTag(nbtToAppend);
        }
        return toReturn;
    }

    public static HashMap<ImmutablePair<Integer, Integer>, Integer> parseReviveInfoFromNBTList(NBTTagList nbtTagList) {
        HashMap<ImmutablePair<Integer, Integer>, Integer> toReturn = new HashMap<>();
        for (int index = 0; index < nbtTagList.tagCount(); index++) {
            NBTTagCompound nbtTagCompound = nbtTagList.getCompoundTagAt(index);
            if (nbtTagCompound.isEmpty()) continue;

            int boxIndex = nbtTagCompound.getInteger("BoxIndex");
            int indexInBox = nbtTagCompound.getInteger("Index");
            int reviveTime = nbtTagCompound.getInteger("ReviveTime");

            toReturn.put(new ImmutablePair<>(boxIndex, indexInBox), reviveTime);
        }
        return toReturn;
    }
}
