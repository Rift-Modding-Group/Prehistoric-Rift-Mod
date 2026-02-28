package anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue;

import net.minecraft.nbt.NBTTagCompound;

public class IntPropertyValue extends PropertyValue<Integer> {
    public IntPropertyValue(String key, Integer defaultValue) {
        super(key, defaultValue);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        nbtTagCompound.setInteger(this.getKey(), this.getValue());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        this.value = nbtTagCompound.getInteger(this.getKey());
    }
}
