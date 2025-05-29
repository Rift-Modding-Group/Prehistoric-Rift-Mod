package anightdazingzoroark.prift.helper;

import java.util.*;

public class WeightedList<E> {
    private final NavigableMap<Integer, E> map = new TreeMap<Integer, E>();
    private final Random random;
    private int total = 0;

    public WeightedList() {
        this.random = new Random();
    }

    public WeightedList<E> add(int weight, E result) {
        if (weight <= 0) return this;
        this.total += weight;
        this.map.put(this.total, result);
        return this;
    }

    public E next() {
        int value = (int)(this.random.nextDouble() * this.total);
        return this.map.higherEntry(value).getValue();
    }

    public List<E> possibleOutcomes() {
        return new ArrayList<>(this.map.values());
    }
}
