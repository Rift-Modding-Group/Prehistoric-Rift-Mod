package anightdazingzoroark.prift.propertySystem.propertyStorage;

import anightdazingzoroark.prift.propertySystem.networking.PropertiesNetworking;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.PropertyValue;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public abstract class AbstractEntityProperties {
    protected final HashMap<String, PropertyValue<?>> propertyValueMap = new HashMap<>();
    @NotNull
    private final String propertyName;
    private final Entity entityHolder;

    public AbstractEntityProperties() {
        this.propertyName = "";
        this.entityHolder = null;
    }

    public AbstractEntityProperties(@NotNull String propertyName, @NotNull Entity entityHolder) {
        this.registerDefaults(entityHolder);
        this.propertyName = propertyName;
        this.entityHolder = entityHolder;
    }

    //-----initialization-----
    protected abstract void registerDefaults(Entity entity);

    //-----setting and getting values-----
    public void put(PropertyValue<?> value) {
        this.propertyValueMap.put(value.getKey(), value);

        //sync to client afterwards from server
        if (this.entityHolder != null && !this.entityHolder.world.isRemote) {
            PropertiesNetworking.sendDelta(
                    this.entityHolder,
                    this.propertyName,
                    value.getKey(),
                    this.writeOneToNBT(value.getKey())
            );
        }
    }

    public boolean has(String key) {
        return this.propertyValueMap.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public final <T> PropertyValue<T> getProperty(String key) {
        return (PropertyValue<T>) this.propertyValueMap.get(key);
    }

    //-----holder related-----
    public @NotNull String getPropertyName() {
        return this.propertyName;
    }

    public @NotNull Entity getEntityHolder() {
        return this.entityHolder;
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
