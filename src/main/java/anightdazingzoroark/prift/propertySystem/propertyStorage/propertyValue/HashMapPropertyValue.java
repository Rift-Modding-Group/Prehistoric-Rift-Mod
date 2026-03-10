package anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.function.Function;

public class HashMapPropertyValue<K, V> extends AbstractPropertyValue<HashMap<K, V>> {
    private final Function<HashMap<K, V>, NBTBase> mapWriter;
    private final Function<NBTBase, HashMap<K, V>> mapReader;

    public HashMapPropertyValue(
            String key,
            Function<HashMap<K, V>, NBTBase> mapWriter,
            Function<NBTBase, HashMap<K, V>> mapReader
    ) {
        super(key, new HashMap<K, V>());
        this.mapWriter = mapWriter;
        this.mapReader = mapReader;
    }

    public void put(K mapKey, V mapValue) {
        this.value.put(mapKey, mapValue);
    }

    public void remove(K mapKey) {
        this.value.remove(mapKey);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        nbtTagCompound.setTag(this.getKey(), this.mapWriter.apply(this.value));
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        this.value = this.mapReader.apply(nbtTagCompound.getTag(this.getKey()));
    }

    @Override
    public Class<HashMap<K, V>> getHeldClass() {
        return (Class<HashMap<K, V>>) (Class<?>) HashMap.class;
    }
}
