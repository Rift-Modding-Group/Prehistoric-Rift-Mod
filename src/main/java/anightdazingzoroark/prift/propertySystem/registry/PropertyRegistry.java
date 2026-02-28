package anightdazingzoroark.prift.propertySystem.registry;

import anightdazingzoroark.prift.propertySystem.propertyStorage.AbstractEntityProperties;
import net.minecraft.entity.Entity;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class PropertyRegistry {
    private static final HashMap<String, ImmutablePair<Class<? extends Entity>, Class<? extends AbstractEntityProperties>>> REGISTRY = new HashMap<>();

    public static void register(String name, Class<? extends Entity> entityClass, Class<? extends AbstractEntityProperties> propertiesClass) {
        REGISTRY.put(name, new ImmutablePair<>(entityClass, propertiesClass));
    }

    public static Class<? extends AbstractEntityProperties> resolve(String key, Entity entity) {
        Class<? extends Entity> entityClass = entity.getClass();
        if (!REGISTRY.containsKey(key)) return null;
        ImmutablePair<Class<? extends Entity>, Class<? extends AbstractEntityProperties>> classPair = REGISTRY.get(key);
        if (!classPair.left.isAssignableFrom(entityClass)) return null;
        else return classPair.right;
    }

    public static Set<String> getAllPropertyNames() {
        return REGISTRY.keySet();
    }

    public static boolean entityCanHaveProperty(String key, Entity entity) {
        if (!REGISTRY.containsKey(key)) return false;
        ImmutablePair<Class<? extends Entity>, Class<? extends AbstractEntityProperties>> pair = REGISTRY.get(key);
        return pair.left.isAssignableFrom(entity.getClass());
    }
}
