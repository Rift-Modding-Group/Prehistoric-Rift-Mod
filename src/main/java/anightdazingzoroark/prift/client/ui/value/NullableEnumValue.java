package anightdazingzoroark.prift.client.ui.value;

import com.cleanroommc.modularui.api.value.IEnumValue;
import com.cleanroommc.modularui.api.value.IIntValue;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class NullableEnumValue<T extends Enum<T>> implements IEnumValue<T>, IIntValue<T> {
    protected final Class<T> enumClass;
    protected T value;

    public NullableEnumValue(Class<T> enumClass, T value) {
        this.enumClass = enumClass;
        this.value = value;
    }

    @Override
    public Class<T> getEnumClass() {
        return this.enumClass;
    }

    @Override
    public int getIntValue() {
        if (this.value == null) return -1;
        return this.value.ordinal();
    }

    @Override
    public void setIntValue(int val) {
        if (val < 0) this.value = null;
        else this.setValue(this.enumClass.getEnumConstants()[val]);
    }

    @Override
    public T getValue() {
        return this.value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public Class<T> getValueType() {
        return this.enumClass;
    }

    public static class Dynamic<T extends Enum<T>> implements IEnumValue<T>, IIntValue<T> {
        protected final Class<T> enumClass;
        protected final Supplier<T> getter;
        protected final Consumer<T> setter;

        public Dynamic(Class<T> enumClass, Supplier<T> getter, Consumer<T> setter) {
            this.enumClass = enumClass;
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public int getIntValue() {
            if (this.getValue() == null) return -1;
            return this.getValue().ordinal();
        }

        @Override
        public void setIntValue(int val) {
            if (val < 0) this.setValue(null);
            else this.setValue(this.enumClass.getEnumConstants()[val]);
        }

        @Override
        public T getValue() {
            return this.getter.get();
        }

        @Override
        public void setValue(T value) {
            this.setter.accept(value);
        }

        @Override
        public Class<T> getEnumClass() {
            return this.enumClass;
        }

        @Override
        public Class<T> getValueType() {
            return this.enumClass;
        }
    }
}
