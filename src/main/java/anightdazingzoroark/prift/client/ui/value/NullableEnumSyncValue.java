package anightdazingzoroark.prift.client.ui.value;

import com.cleanroommc.modularui.api.value.IEnumValue;
import com.cleanroommc.modularui.api.value.sync.IIntSyncValue;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NullableEnumSyncValue<T extends Enum<T>> extends ValueSyncHandler<T> implements IEnumValue<T>, IIntSyncValue<T> {
    private final Class<T> enumClass;
    private final Supplier<T> getter;
    private final Consumer<T> setter;
    protected T cache;

    public NullableEnumSyncValue(@NotNull Class<T> enumClass, @NotNull Supplier<T> getter, @Nullable Consumer<T> setter) {
        this.enumClass = Objects.requireNonNull(enumClass);
        this.getter = Objects.requireNonNull(getter);
        this.setter = setter;
        this.cache = getter.get();
    }

    @Override
    public Class<T> getEnumClass() {
        return this.enumClass;
    }

    @Override
    public void setIntValue(int value, boolean setSource, boolean sync) {
        if (value >= 0) this.setValue(this.enumClass.getEnumConstants()[value], setSource, sync);
        else this.setValue(null, setSource, sync);
    }

    @Override
    public int getIntValue() {
        if (this.cache == null) return -1;
        return this.cache.ordinal();
    }

    @Override
    public void setValue(T value, boolean setSource, boolean sync) {
        this.cache = value;
        if (setSource && this.setter != null) {
            this.setter.accept(value);
        }
        this.onValueChanged();
        if (sync) this.sync();
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || this.getter.get() != this.cache) {
            this.setValue(this.getter.get(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        this.setValue(this.getter.get(), false, true);
    }

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        if (this.cache == null) buffer.writeInt(-1);
        else buffer.writeInt(this.cache.ordinal());
    }

    @Override
    public void read(PacketBuffer buffer) throws IOException {
        int readOrdinal = buffer.readInt();
        T value = readOrdinal >= 0 ? this.enumClass.getEnumConstants()[readOrdinal] : null;
        this.setValue(value, true, false);
    }

    @Override
    public T getValue() {
        return this.cache;
    }

    @Override
    public Class<T> getValueType() {
        return this.enumClass;
    }
}
