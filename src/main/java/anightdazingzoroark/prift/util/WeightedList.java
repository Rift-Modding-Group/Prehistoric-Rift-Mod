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

        //define list to put the result in
        List<T> listToPutIn;
        if (this.map.containsKey(weight)) {
            listToPutIn = this.map.get(weight);
        }
        else listToPutIn = new ArrayList<>();
        //we only want unique objects in weighted lists
        if (!listToPutIn.contains(result)) listToPutIn.add(result);
        this.map.put(this.total, listToPutIn);

        return this;
    }

    @Nullable
    public WeightedList<T> remove(T toRemove) {
        for (Map.Entry<Integer, List<T>> mapEntry : this.map.entrySet()) {
            mapEntry.getValue().remove(toRemove); //im not sure how likely a concurrentmodificationexception will come out of this...
        }
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
