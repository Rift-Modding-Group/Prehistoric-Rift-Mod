package anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue;

import net.minecraft.nbt.NBTTagCompound;

public abstract class PropertyValue<T> {
    private final String key;
    protected T value;

    public PropertyValue(String key, T defaultValue) {
        this.key = key;
        this.value = defaultValue;
    }

    public final String getKey() {
        return this.key;
    }

    public final T getValue() {
        return this.value;
    }

    //does not sync
    public final void setLocal(T newValue) {
        this.value = newValue;
    }

    public abstract void writeToNBT(NBTTagCompound nbtTagCompound);

    public abstract void readFromNBT(NBTTagCompound nbtTagCompound);
}
