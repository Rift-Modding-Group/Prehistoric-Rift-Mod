package anightdazingzoroark.prift.propertySystem.registry;

import anightdazingzoroark.prift.propertySystem.propertyStorage.AbstractEntityProperties;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

public class PropertiesRoot {
    private final Map<String, AbstractEntityProperties> sets = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends AbstractEntityProperties> T getOrCreate(String key, Entity entity) {
        AbstractEntityProperties existing = sets.get(key);
        if (existing != null) return (T) existing;

        Class<? extends AbstractEntityProperties> clazz = PropertyRegistry.resolve(key, entity);
        if (clazz == null) return null;

        AbstractEntityProperties created = newInstance(clazz);
        created.init(entity);
        sets.put(key, created);
        return (T) created;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();
        for (Map.Entry<String, AbstractEntityProperties> entry : sets.entrySet()) {
            toReturn.setTag(entry.getKey(), entry.getValue().writeAllToNBT());
        }
        return toReturn;
    }

    public void readFromNBT(NBTTagCompound nbtTagCompound, Entity entity) {
        for (String key : nbtTagCompound.getKeySet()) {
            NBTTagCompound setTag = nbtTagCompound.getCompoundTag(key);
            AbstractEntityProperties set = this.getOrCreate(key, entity);
            if (set != null) set.readAllFromNBT(setTag);
        }
    }

    private static AbstractEntityProperties newInstance(Class<? extends AbstractEntityProperties> clazz) {
        try {
            return clazz.newInstance();
        }
        catch (Exception ex) {
            throw new RuntimeException("Properties class must have a public no-arg ctor: " + clazz.getName(), ex);
        }
    }
}
