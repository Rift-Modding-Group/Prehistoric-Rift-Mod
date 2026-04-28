package anightdazingzoroark.prift.helper;

import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FixedSizeList<T> {
    private final List<T> list;
    private final int maxSize;
    private final T defaultValue;

    public FixedSizeList(int maxSize) {
        this.maxSize = maxSize;
        this.defaultValue = null;
        this.list = new ArrayList<>(Collections.nCopies(this.maxSize, null));
    }

    public FixedSizeList(int maxSize, T defaultValue) {
        this.maxSize = maxSize;
        this.defaultValue = defaultValue;
        this.list = new ArrayList<>(Collections.nCopies(this.maxSize, this.defaultValue));
    }

    public FixedSizeList(int maxSize, List<T> existingList) {
        this.maxSize = maxSize;
        this.defaultValue = null;
        List<T> listToSet = new ArrayList<>();
        for (int i = 0; i < maxSize; i++) {
            if (i < existingList.size()) listToSet.add(i, existingList.get(i));
            else listToSet.add(null);
        }
        this.list = listToSet;
    }

    public FixedSizeList(int maxSize, List<T> existingList, T defaultValue) {
        this.maxSize = maxSize;
        this.defaultValue = defaultValue;
        List<T> listToSet = new ArrayList<>();
        for (int i = 0; i < maxSize; i++) {
            if (i < existingList.size()) listToSet.add(i, existingList.get(i));
            else listToSet.add(this.defaultValue);
        }
        this.list = listToSet;
    }

    public void add(T value) {
        if (value == null && this.defaultValue != null) throw new UnsupportedOperationException("Cannot add null value to FixedSizeList");
        //find first null position to insert into
        int pos = -1;

        for (int x = 0; x < this.maxSize; x++) {
            if (this.list.get(x) == null || this.list.get(x).equals(this.defaultValue)) {
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
        else throw new UnsupportedOperationException("Invalid position ("+pos+") to add to FixedSizeList of size "+this.maxSize);
    }

    public T get(int pos) {
        if (pos >= 0 && pos < this.maxSize) return this.list.get(pos);
        else throw new UnsupportedOperationException("Invalid position ("+pos+") to get from FixedSizeList");
    }

    public void remove(int pos) {
        if (pos >= 0 && pos < this.maxSize) this.list.set(pos, this.defaultValue);
        else throw new UnsupportedOperationException("Invalid position ("+pos+") to remove from FixedSizeList");
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

    public void clear() {
        for (int i = 0; i < this.maxSize; i++) this.list.set(i, this.defaultValue);
    }

    public boolean isEmpty() {
        //check if entire list is default values
        for (T value : this.list) {
            if (this.defaultValue == null) {
                if (value != null) return false;
            }
            else {
                if (!value.equals(this.defaultValue)) return false;
            }
        }
        return true;
    }

    public int size() {
        return this.maxSize;
    }

    public int indexOf(T value) {
        return this.list.indexOf(value);
    }

    public List<T> getList() {
        return this.list;
    }

    public FixedSizeList<T> sublist(int minRange, int maxRange) {
        if (minRange < 0 || maxRange > this.maxSize) {
            throw new UnsupportedOperationException("Invalid range!");
        }

        int newSize = maxRange - minRange;
        if (newSize < 0) {
            throw new UnsupportedOperationException("Range size of "+newSize+" is invalid!");
        }

        return new FixedSizeList<>(newSize, this.list.subList(minRange, maxRange), this.defaultValue);
    }

    public FixedSizeList<T> combine(FixedSizeList<T> otherList) {
        if (otherList.defaultValue != this.defaultValue) {
            throw new UnsupportedOperationException("Cannot combine FixedSizeList instances with different default values!");
        }
        FixedSizeList<T> toReturn = new FixedSizeList<>(this.maxSize + otherList.maxSize, this.defaultValue);

        //set from this list
        for (int index = 0; index < this.maxSize; index++) {
            T toAdd = this.list.get(index);
            toReturn.set(index, toAdd);
        }

        //set from other list
        for (int index = 0; index < otherList.maxSize; index++) {
            T toAdd = otherList.list.get(index);
            toReturn.set(this.maxSize + index, toAdd);
        }

        return toReturn;
    }

    public String toString() {
        return this.list.toString();
    }
}
