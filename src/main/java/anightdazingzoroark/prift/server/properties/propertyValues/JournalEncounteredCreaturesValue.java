package anightdazingzoroark.prift.server.properties.propertyValues;

import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.PropertyValue;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.Map;

public class JournalEncounteredCreaturesValue extends PropertyValue<Map<RiftCreatureType, Boolean>> {
    public JournalEncounteredCreaturesValue(String key, Map<RiftCreatureType, Boolean> defaultValue) {
        super(key, defaultValue);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        NBTTagList encounteredNBT = new NBTTagList();
        for (Map.Entry<RiftCreatureType, Boolean> encounteredEntry : this.value.entrySet()) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setByte("Creature", (byte) encounteredEntry.getKey().ordinal());
            entry.setBoolean("IsUnlocked", encounteredEntry.getValue());
            encounteredNBT.appendTag(entry);
        }
        nbtTagCompound.setTag(this.getKey(), encounteredNBT);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        this.value.clear();

        NBTTagList encounteredNBT = nbtTagCompound.getTagList(this.getKey(), 10);
        for (int index = 0; index < encounteredNBT.tagCount(); index++) {
            NBTTagCompound entry = encounteredNBT.getCompoundTagAt(index);
            this.value.put(
                    RiftCreatureType.values()[entry.getByte("Creature")],
                    entry.getBoolean("IsUnlocked")
            );
        }
    }
}
