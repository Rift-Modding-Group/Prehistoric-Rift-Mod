package anightdazingzoroark.prift.server.properties.propertyValues;

import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.PropertyValue;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.CreatureBoxStorage;
import net.minecraft.nbt.NBTTagCompound;

public class CreatureBoxStoragePropertyValue extends PropertyValue<CreatureBoxStorage> {
    public CreatureBoxStoragePropertyValue(String key, CreatureBoxStorage defaultValue) {
        super(key, defaultValue);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        nbtTagCompound.setTag(this.getKey(), this.value.writeNBTList());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        this.value.parseNBTList(nbtTagCompound.getTagList(this.getKey(), 10));
    }
}
