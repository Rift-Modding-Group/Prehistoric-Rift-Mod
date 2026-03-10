package anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue;

import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractPropertyValue<T> {
    private final String key;
    protected T value;

    public AbstractPropertyValue(String key, T initValue) {
        this.key = key;
        this.value = initValue;
    }

    public final String getKey() {
        return this.key;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }

    public abstract void writeToNBT(NBTTagCompound nbtTagCompound);

    public abstract void readFromNBT(NBTTagCompound nbtTagCompound);

    public abstract Class<T> getHeldClass();
}
