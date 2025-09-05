package anightdazingzoroark.prift.server.entity.creatureMoveConditions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MoveConditionStorage {
    private final ConditionGroup mainConditionGroup;

    //make an empty one
    public MoveConditionStorage() {
        this.mainConditionGroup = null;
    }

    public MoveConditionStorage(ConditionContainer singleCondition) {
        this.mainConditionGroup = new ConditionAllSelect(singleCondition);
    }

    public MoveConditionStorage(ConditionGroup conditions) {
        this.mainConditionGroup = conditions;
    }

    public ConditionGroup getMainConditionGroup() {
        return this.mainConditionGroup;
    }

    public static class ConditionBase {}

    public static class ConditionGroup extends ConditionBase {
        //condition groups can only contain other condition groups or ConditionContainers
        protected List<ConditionBase> conditions;

        public ConditionGroup(ConditionBase... conditions) {
            this.conditions = Arrays.asList(conditions);
        }

        public List<ConditionBase> getConditions() {
            return this.conditions;
        }
    }

    public static class ConditionAnySelect extends ConditionGroup {
        public ConditionAnySelect(ConditionBase... conditions) {
            super(conditions);
        }
    }

    public static class ConditionAllSelect extends ConditionGroup {
        public ConditionAllSelect(ConditionBase... conditions) {
            super(conditions);
        }
    }

    public static class ConditionContainer extends ConditionBase {
        public final MoveCondition condition;
        public final boolean negate;

        public ConditionContainer(MoveCondition condition) {
            this(condition, false);
        }

        public ConditionContainer(MoveCondition condition, boolean negate) {
            this.condition = condition;
            this.negate = negate;
        }
    }
}