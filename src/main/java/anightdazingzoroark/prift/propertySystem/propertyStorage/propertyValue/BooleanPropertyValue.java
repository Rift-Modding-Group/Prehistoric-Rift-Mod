package anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue;

import net.minecraft.nbt.NBTTagCompound;

public class BooleanPropertyValue extends PropertyValue<Boolean> {
    public BooleanPropertyValue(String key, Boolean defaultValue) {
        super(key, defaultValue);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        nbtTagCompound.setBoolean(this.getKey(), this.value);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        this.value = nbtTagCompound.getBoolean(this.getKey());
    }
}
