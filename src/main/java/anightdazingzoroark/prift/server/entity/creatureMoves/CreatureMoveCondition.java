package anightdazingzoroark.prift.server.entity.creatureMoves;

import java.util.ArrayList;
import java.util.List;

//the canBeExecutedUnmounted method within each CreatureMove and this are completely different
//CreatureMoveCondition checks go first and determine whether or not the creature uses a move
//canBeExecutedUnmounted checks for which among the available moves are to be selected when in move use mode
public class CreatureMoveCondition {
    public final List<Condition> conditions = new ArrayList<>();
    private int rngChance; //how likely move will be used, move has 1 / rngChance to be used
    private int tickInterval; //interval in ticks to apply the condition
    private boolean checkForTarget; //whether or not target being detected is a trigger, pretty obvious lmao
    private boolean checkForUncloaked; //whether or not being uncloaked is a trigger
    private boolean checkForHit; //whether or not being hit is a trigger
    private double belowHealthPercentage; //whether or not users health in percent form is this or below
    private boolean canLeapOverLedge; //for navigation, for making creatures use leap related moves to cross gaps

    //rng has no Condition attached to it
    //it is meant to be attached to other conditions
    public CreatureMoveCondition setRNGChance(int value) {
        this.rngChance = value;
        return this;
    }

    public int getRNGChance() {
        return this.rngChance;
    }

    public CreatureMoveCondition setInterval(int value) {
        this.tickInterval = value;
        this.conditions.add(Condition.INTERVAL);
        return this;
    }

    public int getTickInterval() {
        return this.tickInterval;
    }

    public CreatureMoveCondition setCheckForTarget() {
        this.checkForTarget = true;
        this.conditions.add(Condition.CHECK_TARGET);
        return this;
    }

    public boolean getCheckForTarget() {
        return this.checkForTarget;
    }

    public CreatureMoveCondition setCheckForUncloaked() {
        this.checkForUncloaked = true;
        this.conditions.add(Condition.CHECK_UNCLOAKED);
        return this;
    }

    public boolean getCheckForUncloaked() {
        return this.checkForUncloaked;
    }

    public CreatureMoveCondition setCheckForHit() {
        this.checkForHit = true;
        this.conditions.add(Condition.CHECK_HIT);
        return this;
    }

    public boolean getCheckForHit() {
        return this.checkForHit;
    }

    public CreatureMoveCondition setBelowHealthPercentage(double percentage) {
        this.belowHealthPercentage = percentage;
        this.conditions.add(Condition.HEALTH_BELOW_VALUE);
        return this;
    }

    public double getBelowHealthPercentage() {
        return this.belowHealthPercentage;
    }

    public CreatureMoveCondition setCanLeapOverLedge() {
        this.canLeapOverLedge = true;
        this.conditions.add(Condition.LEAP_OVER_LEDGE);
        return this;
    }

    public boolean getCanLeapOverLedge() {
        return this.canLeapOverLedge;
    }

    public enum Condition {
        //order of values determines priority
        //higher position, higher priority
        HEALTH_BELOW_VALUE,
        CHECK_UNCLOAKED,
        CHECK_HIT,
        INTERVAL,
        LEAP_OVER_LEDGE,
        CHECK_TARGET
    }
}
