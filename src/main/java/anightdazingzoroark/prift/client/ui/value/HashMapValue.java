package anightdazingzoroark.prift.client.ui.value;

import com.cleanroommc.modularui.api.value.IValue;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HashMapValue<K, V> implements IValue<HashMap<K, V>> {
    @NotNull
    private HashMap<K, V> hashMap = new HashMap<>();

    @Override
    public HashMap<K, V> getValue() {
        return this.hashMap;
    }

    @Override
    public void setValue(HashMap<K, V> value) {
        this.hashMap = value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<HashMap<K, V>> getValueType() {
        return (Class<HashMap<K, V>>) (Class<?>) HashMap.class;
    }

    public static class Dynamic<K, V> implements IValue<HashMap<K, V>> {
        private final Supplier<HashMap<K, V>> getter;
        private final Consumer<HashMap<K, V>> setter;

        public Dynamic(Supplier<HashMap<K, V>> getter, Consumer<HashMap<K, V>> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public HashMap<K, V> getValue() {
            return this.getter.get();
        }

        @Override
        public void setValue(HashMap<K, V> value) {
            this.setter.accept(value);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<HashMap<K, V>> getValueType() {
            return (Class<HashMap<K, V>>) (Class<?>) HashMap.class;
        }
    }
}
