package anightdazingzoroark.prift.helper;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basically like a HashMap, but with support for indexes
 * */
public class IndexedMap<K, V> {
    private final Map<K, V> map = new HashMap<>();
    private final Map<Integer, K> mapOrder = new HashMap<>();
    private int lastIndex;

    public void put(K key, V value) {
        if (!this.map.containsKey(key)) this.mapOrder.put(this.lastIndex++, key);
        this.map.put(key, value);
    }

    public Map<K, V> getMap() {
        return Map.copyOf(this.map);
    }

    public V get(K key) {
        return this.map.get(key);
    }

    public K getKeyByIndex(int index) {
        return this.mapOrder.get(index);
    }

    public V getByIndex(int index) {
        if (index >= 0 && index < this.lastIndex) {
            K key = this.mapOrder.get(index);
            return this.map.get(key);
        }
        else throw new UnsupportedOperationException("Invalid position ("+index+") to get from IndexedMap");
    }

    public V remove(K key) {
        if (!this.map.containsKey(key)) throw new UnsupportedOperationException("Cannot find key "+key+" in IndexedMap");

        V toReturn = this.map.remove(key);
        this.mapOrder.remove(this.lastIndex--);

        return toReturn;
    }

    public boolean containsKey(K key) {
        return this.map.containsKey(key);
    }

    public boolean containsValue(V value) {
        return this.map.containsValue(value);
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public int getIndex(K key) {
        for (Map.Entry<Integer, K> mapOrderEntry : this.mapOrder.entrySet()) {
            if (mapOrderEntry.getValue().equals(key)) return mapOrderEntry.getKey();
        }
        throw new UnsupportedOperationException("Cannot find key "+key+" in IndexedMap");
    }

    public int size() {
        return this.lastIndex;
    }

    //basically like sublists but for this
    public IndexedMap<K, V> subMap(int minRange, int maxRange) {
        if (minRange < 0 || maxRange > this.lastIndex) {
            throw new UnsupportedOperationException("Invalid range!");
        }

        int newSize = maxRange - minRange;
        if (newSize < 0) {
            throw new UnsupportedOperationException("Range size of "+newSize+" is invalid!");
        }

        IndexedMap<K, V> toReturn = new IndexedMap<>();
        for (int index = minRange; index < maxRange; index++) {
            K key = this.mapOrder.get(index);
            toReturn.put(key, this.map.get(key));
        }
        return toReturn;
    }

    //returns a list of immutable pairs that represent each entry in map
    //ordered based on mapOrder
    public List<ImmutablePair<K, V>> getEntryList() {
        List<ImmutablePair<K, V>> toReturn = new ArrayList<>();

        for (int index = 0; index < this.lastIndex; index++) {
            K key = this.mapOrder.get(index);
            toReturn.add(new ImmutablePair<>(key, this.map.get(key)));
        }

        return toReturn;
    }

    //make an IndexedMap on the go
    public static class Builder<K, V> {
        private final IndexedMap<K, V> toReturn = new IndexedMap<>();

        public Builder<K, V> put(K key, V value) {
            this.toReturn.put(key, value);
            return this;
        }

        public IndexedMap<K, V> build() {
            return this.toReturn;
        }
    }
}
