package anightdazingzoroark.prift.server.entity.creatureMoves;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//this is for storing move conditions
//the move conditions are ordered based on their priority
//which is based on their ordinal order
public class CreatureMoveConditionStack {
    private final List<CreatureMoveCondition.Condition> conditions = new ArrayList<>();

    public void addCondition(CreatureMoveCondition.Condition condition) {
        //check uniqueness, skip entirely if its already in list
        if (this.conditions.contains(condition)) return;

        //add to conditions list first
        this.conditions.add(condition);

        //now contents from highest priority
        //to lowest priority
        this.conditions.sort(Comparator.naturalOrder());
    }

    public void removeCondition(CreatureMoveCondition.Condition condition) {
        //check availability, skip entirely if not in list
        if (!this.conditions.contains(condition)) return;

        //now remove from list
        this.conditions.remove(condition);
    }

    public void removeHead() {
        if (!this.conditions.isEmpty()) this.conditions.remove(0);
    }

    public List<CreatureMoveCondition.Condition> getConditions() {
        return this.conditions;
    }

    public CreatureMoveCondition.Condition getHead() {
        if (!this.conditions.isEmpty()) return this.conditions.get(0);
        return null;
    }
}
