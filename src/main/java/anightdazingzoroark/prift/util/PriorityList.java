package anightdazingzoroark.prift.util;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PriorityList<T> {
    private final Map<Integer, List<T>> map = new HashMap<>();
    private final Random random = new Random();

    public PriorityList<T> add(int priority, T item) {
        if (priority <= 0) return this;

        //if list exists at said priority, put it there
        if (this.map.containsKey(priority)) {
            List<T> listToPutIn = this.map.get(priority);
            if (!listToPutIn.contains(item)) listToPutIn.add(item);
        }
        //otherwise, create it and said priority
        else {
            List<T> listToPutIn = new ArrayList<>();
            listToPutIn.add(item);
            this.map.put(priority, listToPutIn);
        }

        return this;
    }

    //remove item from all lists in map as well as delete empty lists in map
    public PriorityList<T> remove(T toRemove) {
        this.map.entrySet().removeIf(entry -> {
            List<T> list = entry.getValue();
            list.remove(toRemove);
            return list.isEmpty();
        });

        return this;
    }

    @Nullable
    public T next() {
        int highestPriority = Integer.MAX_VALUE;

        for (int priority : this.map.keySet()) {
            if (priority < highestPriority) highestPriority = priority;
        }

        List<T> resultList = this.map.get(highestPriority);
        if (resultList == null || resultList.isEmpty()) return null;

        return resultList.get(this.random.nextInt(resultList.size()));
    }

    @Override
    public String toString() {
        if (this.map.isEmpty()) return "{}";

        StringBuilder builder = new StringBuilder("{");
        List<Integer> priorities = new ArrayList<>(this.map.keySet());
        Collections.sort(priorities);

        for (int i = 0; i < priorities.size(); i++) {
            int priority = priorities.get(i);
            builder.append(priority).append("=").append(this.map.get(priority));
            if (i < priorities.size() - 1) builder.append(", ");
        }

        return builder.append("}").toString();
    }
}
