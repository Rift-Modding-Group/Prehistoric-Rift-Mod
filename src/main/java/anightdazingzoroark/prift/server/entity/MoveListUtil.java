package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MoveListUtil {
    public static NBTTagCompound getNBTFromFixedSizeListCreatureMove(FixedSizeList<CreatureMove> creatureMoveFixedSizeList) {
        NBTTagCompound toReturn = new NBTTagCompound();

        toReturn.setInteger("Size", creatureMoveFixedSizeList.size());

        NBTTagList moveListNBT = new NBTTagList();
        for (int i = 0; i < creatureMoveFixedSizeList.size(); i++) {
            CreatureMove creatureMove = creatureMoveFixedSizeList.get(i);
            if (creatureMove == null) continue;
            NBTTagCompound moveNBT = new NBTTagCompound();
            moveNBT.setInteger("Move", creatureMove.ordinal());
            moveNBT.setInteger("Index", i);
            moveListNBT.appendTag(moveNBT);
        }
        toReturn.setTag("MoveList", moveListNBT);

        return toReturn;
    }

    public static FixedSizeList<CreatureMove> getFixedSizeListCreatureMoveFromNBT(NBTTagCompound nbtTagCompound) {
        FixedSizeList<CreatureMove> toReturn = new FixedSizeList<>(nbtTagCompound.getInteger("Size"));

        NBTTagList moveListNBT = nbtTagCompound.getTagList("MoveList", 10);
        for (int i = 0; i < moveListNBT.tagCount(); i++) {
            NBTTagCompound moveNBT = moveListNBT.getCompoundTagAt(i);
            toReturn.set(
                    moveNBT.getInteger("Index"),
                    CreatureMove.values()[moveNBT.getInteger("Move")]
            );
        }

        return toReturn;
    }

    public static NBTTagCompound getNBTFromListCreatureMove(List<CreatureMove> creatureMoveList) {
        NBTTagCompound toReturn = new NBTTagCompound();

        NBTTagList moveListNBT = new NBTTagList();
        for (int i = 0; i < creatureMoveList.size(); i++) {
            CreatureMove creatureMove = creatureMoveList.get(i);
            if (creatureMove == null) continue;
            NBTTagCompound moveNBT = new NBTTagCompound();
            moveNBT.setInteger("Move", creatureMove.ordinal());
            moveNBT.setInteger("Index", i);
            moveListNBT.appendTag(moveNBT);
        }
        toReturn.setTag("MoveList", moveListNBT);

        return toReturn;
    }

    public static List<CreatureMove> getListCreatureMoveFromNBT(NBTTagCompound nbtTagCompound) {
        List<CreatureMove> toReturn = new ArrayList<>();

        NBTTagList moveListNBT = nbtTagCompound.getTagList("MoveList", 10);

        //get move nbts first to then sort by index, smallest to biggest
        List<NBTTagCompound> tentativeMoveList = new ArrayList<>();
        for (int i = 0; i < moveListNBT.tagCount(); i++) {
            NBTTagCompound moveNBT = moveListNBT.getCompoundTagAt(i);
            tentativeMoveList.add(moveNBT);
        }
        tentativeMoveList.sort(Comparator.comparingInt(nbt -> nbt.getInteger("Index")));

        //now put them all in toReturn after sorting
        for (NBTTagCompound moveNBT : tentativeMoveList) {
            toReturn.add(CreatureMove.values()[moveNBT.getInteger("Move")]);
        }

        return toReturn;
    }
}
