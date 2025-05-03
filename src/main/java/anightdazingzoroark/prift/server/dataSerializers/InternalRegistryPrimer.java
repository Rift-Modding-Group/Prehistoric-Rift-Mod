package anightdazingzoroark.prift.server.dataSerializers;

import com.google.common.collect.Lists;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InternalRegistryPrimer {
    private Map<Class<?>, List<IForgeRegistryEntry<?>>> primed = new HashMap<>();

    public <V extends IForgeRegistryEntry<V>> V register(V entry) {
        Class<V> type = entry.getRegistryType();
        List<IForgeRegistryEntry<?>> entries = primed.computeIfAbsent(type, k -> Lists.newLinkedList());
        entries.add(entry);
        return entry;
    }

    public <T extends IForgeRegistryEntry<T>> List<?> getEntries(Class<T> type) {
        return primed.get(type);
    }

    public void wipe(Class<?> type) {
        primed.remove(type);
    }
}
