package anightdazingzoroark.prift.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FixedSizeList<T> {
    private final List<T> list;
    private final int maxSize;
    private final T defaultValue;

    public FixedSizeList(int maxSize) {
        this(maxSize, null);
    }

    public FixedSizeList(int maxSize, T defaultValue) {
        this.maxSize = maxSize;
        this.defaultValue = defaultValue;
        this.list = new ArrayList<>(Collections.nCopies(this.maxSize, this.defaultValue));
    }

    public void add(T value) {
        if (value == null && this.defaultValue != null) throw new UnsupportedOperationException("Cannot add null value to FixedSizeList");
        //find first null position to insert into
        int pos = -1;

        for (int x = 0; x < this.maxSize; x++) {
            if (this.list.get(x).equals(this.defaultValue)) {
                pos = x;
                break;
            }
        }

        if (pos >= 0) this.list.set(pos, value);
        else throw new UnsupportedOperationException("Cannot add any more values to FixedSizeList");
    }

    public void set(int pos, T value) {
        if (value == null && this.defaultValue != null) throw new UnsupportedOperationException("Cannot set null value in FixedSizeList");
        if (pos >= 0 && pos < this.maxSize) this.list.set(pos, value);
        else throw new UnsupportedOperationException("Invalid position to add to FixedSizeList");
    }

    public T get(int pos) {
        if (pos >= 0 && pos < this.maxSize) return this.list.get(pos);
        else throw new UnsupportedOperationException("Invalid position to get from FixedSizeList");
    }

    public void remove(int pos) {
        if (pos >= 0 && pos < this.maxSize) this.list.set(pos, this.defaultValue);
        else throw new UnsupportedOperationException("Invalid position to remove from FixedSizeList");
    }

    public void remove(T value) {
        //search within list first for value
        int pos = -1;

        for (int x = 0; x < this.maxSize; x++) {
            if (this.list.get(x) != null && this.list.get(x).equals(value)) {
                pos = x;
                break;
            }
        }

        if (pos >= 0) this.list.set(pos, this.defaultValue);
        else throw new UnsupportedOperationException("Cannot remove value from FixedSizeList");
    }

    public boolean contains(T value) {
        return this.list.contains(value);
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public int size() {
        return this.maxSize;
    }

    public List<T> getList() {
        return this.list;
    }

    public String toString() {
        return this.list.toString();
    }
}
