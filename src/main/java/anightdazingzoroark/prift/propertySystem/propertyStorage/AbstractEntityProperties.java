package anightdazingzoroark.prift.propertySystem.propertyStorage;

import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.PropertyValue;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;

public abstract class AbstractEntityProperties {
    protected final HashMap<String, PropertyValue<?>> propertyValueMap = new HashMap<>();
    private boolean initialized;

    //subclasses register the properties they want available
    protected abstract void registerDefaults(Entity entity);

    public final void init(Entity entity) {
        if (!this.initialized) {
            this.initialized = true;
            this.registerDefaults(entity);
        }
    }

    public void put(PropertyValue<?> value) {
        this.propertyValueMap.put(value.getKey(), value);
    }

    public boolean has(String key) {
        return this.propertyValueMap.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public final <T> PropertyValue<T> getProperty(String key) {
        return (PropertyValue<T>) this.propertyValueMap.get(key);
    }

    //-----save all properties-----
    public NBTTagCompound writeAllToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        for (PropertyValue<?> propertyValue : this.propertyValueMap.values()) propertyValue.writeToNBT(tag);
        return tag;
    }

    //load all properties
    public void readAllFromNBT(NBTTagCompound tag) {
        for (PropertyValue<?> propertyValue : this.propertyValueMap.values()) propertyValue.readFromNBT(tag);
    }

    //save one property by key (for delta sync)
    public NBTTagCompound writeOneToNBT(String key) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        PropertyValue<?> propertyValue = this.propertyValueMap.get(key);
        if (propertyValue != null) propertyValue.writeToNBT(nbtTagCompound);
        return nbtTagCompound;
    }

    public void readOneFromNBT(NBTTagCompound tag, String key) {
        PropertyValue<?> propertyValue = this.propertyValueMap.get(key);
        if (propertyValue != null) propertyValue.readFromNBT(tag);
    }
}
