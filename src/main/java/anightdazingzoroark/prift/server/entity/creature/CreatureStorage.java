package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.util.FixedSizeList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class that defines storage for creatures
 * */
public class CreatureStorage {
    @NotNull
    private final FixedSizeList<CreatureNBT> creatureNBTList;

    public CreatureStorage(int size) {
        this.creatureNBTList = new FixedSizeList<>(size, CreatureNBT.EMPTY_NBT);
    }

    @NotNull
    public CreatureNBT getCreature(int index) {
        return this.creatureNBTList.get(index);
    }

    public void setCreature(int index, @NotNull CreatureNBT creatureNBT) {
        this.creatureNBTList.set(index, creatureNBT);
    }

    public void removeCreature(int index) {
        this.setCreature(index, CreatureNBT.EMPTY_NBT);
    }

    public int getSize() {
        return this.creatureNBTList.size();
    }

    @NotNull
    public NBTTagCompound getAsNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();
        NBTTagList tagList = new NBTTagList();

        for (int index = 0; index < this.creatureNBTList.size(); index++) {
            NBTTagCompound nbtToAdd = new NBTTagCompound();
            nbtToAdd.setInteger("Index", index);
            nbtToAdd.setTag("Creature", this.creatureNBTList.get(index).nbtTagCompound());
            tagList.appendTag(nbtToAdd);
        }
        toReturn.setTag("StoredCreatures", tagList);
        return toReturn;
    }

    public void readFromNBT(@NotNull NBTTagCompound nbtTagCompound) {
        this.creatureNBTList.clear();

        NBTTagList tagList = nbtTagCompound.getTagList("StoredCreatures", 10);
        for (int index = 0; index < tagList.tagCount(); index++) {
            NBTTagCompound nbtToGet = tagList.getCompoundTagAt(index);
            if (nbtToGet == null || nbtToGet.isEmpty()) continue;

            int storageIndex = nbtToGet.getInteger("Index");
            if (storageIndex < 0 || storageIndex >= this.creatureNBTList.size()) continue;

            NBTTagCompound rawCreatureNBT = nbtToGet.getCompoundTag("Creature");
            CreatureNBT creatureNBT = !rawCreatureNBT.isEmpty() ? new CreatureNBT(rawCreatureNBT) : CreatureNBT.EMPTY_NBT;

            this.creatureNBTList.set(storageIndex, creatureNBT);
        }
    }
}
