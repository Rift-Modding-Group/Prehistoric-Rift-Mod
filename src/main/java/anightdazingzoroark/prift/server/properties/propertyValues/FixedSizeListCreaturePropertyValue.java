package anightdazingzoroark.prift.server.properties.propertyValues;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.PropertyValue;
import anightdazingzoroark.prift.helper.CreatureNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class FixedSizeListCreaturePropertyValue extends PropertyValue<FixedSizeList<CreatureNBT>> {
    public FixedSizeListCreaturePropertyValue(String key, FixedSizeList<CreatureNBT> defaultValue) {
        super(key, defaultValue);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        NBTTagList partyTagList = new NBTTagList();
        for (int index = 0; index < this.value.size(); index++) {
            CreatureNBT creatureNBT = this.value.get(index);
            if (creatureNBT.nbtIsEmpty()) continue;
            NBTTagCompound toAppend = new NBTTagCompound();
            toAppend.setInteger("Index", index);
            toAppend.setTag("Creature", creatureNBT.getCreatureNBT());
            partyTagList.appendTag(toAppend);
        }
        nbtTagCompound.setTag(this.getKey(), partyTagList);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        this.value.clear();
        NBTTagList partyTagList = nbtTagCompound.getTagList(this.getKey(), 10);
        for (int index = 0; index < partyTagList.tagCount(); index++) {
            NBTTagCompound tagCompound = partyTagList.getCompoundTagAt(index);
            int partyMemIndex = tagCompound.getInteger("Index");
            CreatureNBT creatureNBT = new CreatureNBT(tagCompound.getCompoundTag("Creature"));
            this.value.set(partyMemIndex, creatureNBT);
        }
    }
}
