package anightdazingzoroark.prift.util;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public class WeightedList<T> {
    private final NavigableMap<Integer, List<T>> map = new TreeMap<Integer, List<T>>();
    private final Random random = new Random();
    private int total = 0;

    public WeightedList<T> add(int weight, T result) {
        if (weight <= 0) return this;
        this.total += weight;

        List<T> listToPutIn;
        if (this.map.containsKey(weight)) {
            listToPutIn = this.map.get(weight);
        }
        else listToPutIn = new ArrayList<>();
        listToPutIn.add(result);
        this.map.put(this.total, listToPutIn);

        return this;
    }

    @Nullable
    public T next() {
        int value = (int)(this.random.nextDouble() * this.total);
        Map.Entry<Integer, List<T>> higherEntry = this.map.higherEntry(value);
        if (higherEntry == null) return null;

        List<T> nextListValue = higherEntry.getValue();
        if (nextListValue.isEmpty()) return null;

        return nextListValue.get(this.random.nextInt(nextListValue.size()));
    }
}
