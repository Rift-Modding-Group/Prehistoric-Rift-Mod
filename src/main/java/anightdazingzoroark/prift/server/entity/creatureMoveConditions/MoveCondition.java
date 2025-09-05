package anightdazingzoroark.prift.server.entity.creatureMoveConditions;

import anightdazingzoroark.prift.server.enums.MobSize;
import net.minecraft.potion.Potion;

import java.util.List;

public class MoveCondition {
    //static methods and variables for defining conditions
    public static MoveCondition healthInValueIntervalCondition(float minPercentage, float maxPercentage) {
        MoveCondition toReturn = new MoveCondition(Condition.HEALTH_IN_VALUE_INTERVAL);
        toReturn.healthValueInterval = new Float[]{minPercentage, maxPercentage};
        return toReturn;
    }

    public static MoveCondition healthInValueInterval(float maxPercentage) {
        MoveCondition toReturn = new MoveCondition(Condition.HEALTH_IN_VALUE_INTERVAL);
        toReturn.healthValueInterval = new Float[]{0f, maxPercentage};
        return toReturn;
    }
    public static MoveCondition CHECK_UNCLOAKED = new MoveCondition(Condition.CHECK_UNCLOAKED);
    public static MoveCondition IS_HIT = new MoveCondition(Condition.IS_HIT);
    public static MoveCondition LEAP_OVER_EDGE = new MoveCondition(Condition.LEAP_OVER_LEDGE);
    public static MoveCondition targetTooCloseCondition(int radius) {
        MoveCondition toReturn = new MoveCondition(Condition.TARGET_TOO_CLOSE);
        toReturn.targetRadius = radius;
        return toReturn;
    }
    public static MoveCondition BREAK_BLOCKS = new MoveCondition(Condition.BREAK_BLOCKS);
    public static MoveCondition hasNoEffect(List<Potion> effectsToFind) {
        MoveCondition toReturn = new MoveCondition(Condition.TARGET_HAS_NO_EFFECT);
        toReturn.effectMissingToCheck = effectsToFind;
        return toReturn;
    }
    public static MoveCondition HAS_TARGET = new MoveCondition(Condition.HAS_TARGET);
    public static MoveCondition interval(int minInterval, int maxInterval) {
        MoveCondition toReturn = new MoveCondition(Condition.INTERVAL);
        toReturn.tickTimeInterval = new Integer[]{minInterval, maxInterval};
        return toReturn;
    }
    public static MoveCondition interval(int interval) {
        MoveCondition toReturn = new MoveCondition(Condition.INTERVAL);
        toReturn.tickTimeInterval = new Integer[]{interval, interval};
        return toReturn;
    }
    public static MoveCondition rng(int maxRoll) {
        MoveCondition toReturn = new MoveCondition(Condition.RANDOM);
        toReturn.maxRandomRoll = maxRoll;
        return toReturn;
    }
    public static MoveCondition RESTRICT_BY_SIZE = new MoveCondition(Condition.RESTRICT_BY_SIZE);

    //all the data involving conditions
    public final Condition condition;

    //for health in value interval
    public Float[] healthValueInterval;

    //for target being too close
    public Integer targetRadius;

    //for no effect
    public List<Potion> effectMissingToCheck;

    //for interval
    public Integer[] tickTimeInterval;

    //for rng
    public Integer maxRandomRoll;

    //for target size interval
    public MobSize[] mobSizeInterval;

    private MoveCondition(Condition condition) {
        this.condition = condition;
    }

    public enum Condition {
        //order of values determines priority
        //higher position, higher priority
        HEALTH_IN_VALUE_INTERVAL,
        CHECK_UNCLOAKED,
        IS_HIT,
        LEAP_OVER_LEDGE,
        TARGET_TOO_CLOSE,
        BREAK_BLOCKS,
        TARGET_HAS_NO_EFFECT,
        HAS_TARGET,
        INTERVAL,
        //everything below has no priority, so they always get checked in addition to current condition
        RANDOM(false),
        RESTRICT_BY_SIZE(false);

        public final boolean hasPriority;

        Condition() {
            this(true);
        }

        Condition(boolean hasPriority) {
            this.hasPriority = hasPriority;
        }
    }
}
