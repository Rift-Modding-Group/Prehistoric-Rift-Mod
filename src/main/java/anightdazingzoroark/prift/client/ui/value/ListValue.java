package anightdazingzoroark.prift.client.ui.value;

import com.cleanroommc.modularui.api.value.IValue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ListValue<T> implements IValue<List<T>> {
    @NotNull
    private List<T> list = new ArrayList<>();

    @Override
    public List<T> getValue() {
        return this.list;
    }

    @Override
    public void setValue(@NotNull List<T> value) {
        this.list = value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<List<T>> getValueType() {
        return (Class<List<T>>) (Class<?>) List.class;
    }
}
