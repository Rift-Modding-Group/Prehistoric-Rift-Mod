package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.util.FixedSizeList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Helper class that defines storage for creatures
 * */
public class CreatureStorage {
    @NotNull
    private final String identifier;
    @NotNull
    private final FixedSizeList<CreatureNBT> creatureNBTList;

    public CreatureStorage(@NotNull String identifier, int size) {
        this.identifier = Objects.requireNonNull(identifier, "Storage identifier must not be null");
        if (identifier.isBlank()) throw new IllegalArgumentException("Storage identifier must not be blank");
        this.creatureNBTList = new FixedSizeList<>(size, CreatureNBT.EMPTY_NBT);
    }

    @NotNull
    public String getIdentifier() {
        return this.identifier;
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

    //-----swap related stuff-----
    /**
     * Returns -1 when there is no room.
     * */
    public int getFirstEmptySlot() {
        for (int slot = 0; slot < this.getSize(); slot++) {
            if (this.getCreature(slot).nbtTagCompound().isEmpty()) return slot;
        }
        return -1;
    }

    /**
     * Exchanges two slots, including empty slots, without changing creature data.
     * Gameplay transfers should use CreatureStorageTransfer to apply ownership and deployment rules.
     */
    public boolean swapWith(int sourceSlot, @NotNull CreatureStorage destination, int destinationSlot) {
        if (sourceSlot < 0 || sourceSlot >= this.getSize()
                || destinationSlot < 0 || destinationSlot >= destination.getSize()
                || this == destination && sourceSlot == destinationSlot) return false;
        CreatureNBT source = this.getCreature(sourceSlot);
        CreatureNBT target = destination.getCreature(destinationSlot);
        this.setCreature(sourceSlot, target);
        destination.setCreature(destinationSlot, source);
        return true;
    }

    //-----nbt stuff-----
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
        // The owner declares the identifier; loading creature data does not replace that identity.
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
