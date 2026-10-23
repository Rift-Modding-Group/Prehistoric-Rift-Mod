package anightdazingzoroark.prift.api.creature.builder;

import anightdazingzoroark.prift.api.creature.CreatureMoveResult;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.DetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HorizontalDistanceFromUserDetectionRule;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Creature-level AI policy for choosing moves.
 * */
public class CreatureMoveSelectorBuilder {
    //extremely important
    protected boolean locked;

    //general storage of move rules
    private final List<MoveRule> moveRules = new ArrayList<>();

    /**
     * This locks this object so that when accessing any instances of this, it can never be modified ever
     * */
    public void lock() {
        this.locked = true;
    }

    public CreatureMoveSelectorBuilder setMoveRule(MoveRuleBuilder moveRuleBuilder) {
        this.checkIfLocked();

        moveRuleBuilder.lock();
        this.moveRules.add(new MoveRule(CreatureMoveResult.USE_MOVE, moveRuleBuilder));
        return this;
    }

    /**
     * Make it so that sprinting can be used as an attack by this creature.
     * Note that its only for when its on its own, when controlled by a rider
     * it can spring to attack when commanded to (by simply sprinting lol)
     * */
    public CreatureMoveSelectorBuilder setCanSprintToAttack(int priority, double minDist, double maxDist) {
        this.checkIfLocked();
        if (minDist > maxDist) throw new IllegalArgumentException(minDist+" is greater than "+maxDist+"!");

        MoveRuleBuilder sprintMoveRuleBuilder = new MoveRuleBuilder("")
                .setPriorityPredicate((creature, target) -> {
                    if (target == null || !target.isEntityAlive()) return -1;
                    if (!creature.canSprintToAttack() && !creature.atFrustrationThreshold()) return -1;
                    boolean outsideCreatureAABB = !creature.getEntityBoundingBox().grow(1e-5D).intersects(target.getEntityBoundingBox());
                    boolean outsideInnerBound = !creature.getEntityBoundingBox().grow(minDist).grow(1e-5D).intersects(target.getEntityBoundingBox());
                    boolean withinOuterBound = creature.getEntityBoundingBox().grow(maxDist).grow(1e-5D).intersects(target.getEntityBoundingBox());

                    if (!outsideCreatureAABB || !withinOuterBound || !outsideInnerBound) return -1;
                    return creature.hasStraightWalkingPathTo(target) ? priority : -1;
                })
                .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(minDist, maxDist))
                .setUseWhenFrustrated();
        sprintMoveRuleBuilder.lock();

        this.moveRules.add(new MoveRule(CreatureMoveResult.SPRINT, sprintMoveRuleBuilder));
        return this;
    }

    /**
     * Make it so that leaping can be used as an attack by this creature.
     * setting requiresTargetContact to true makes it so that it deals
     * damage upon touching the target when leaping. Otherwise it just
     * jumps to the target.
     * */
    public CreatureMoveSelectorBuilder setCanLeapToAttack(int priority, double minDist, double maxDist, boolean requiresTargetContact) {
        this.checkIfLocked();
        if (minDist > maxDist) throw new IllegalArgumentException(minDist+" is greater than "+maxDist+"!");

        HorizontalDistanceFromUserDetectionRule detectionRule = new HorizontalDistanceFromUserDetectionRule(minDist, maxDist);
        LeapMoveRuleBuilder leapMoveRuleBuilder = new LeapMoveRuleBuilder(requiresTargetContact).setLeapPriorityPredicate(priority, detectionRule);
        leapMoveRuleBuilder.setUseWhenFrustrated();
        leapMoveRuleBuilder.lock();

        this.moveRules.add(new MoveRule(CreatureMoveResult.LEAP, leapMoveRuleBuilder));
        return this;
    }

    @NotNull
    public List<MoveRule> getMoveRules() {
        return List.copyOf(this.moveRules);
    }

    /**
     * Put this on every setter in builder to protect from post-creation editing
     * */
    protected void checkIfLocked() {
        if (this.locked) throw new IllegalCallerException("A setter for a move selector builder cannot be called after the move selector is registered!");
    }

    public record MoveRule(@NotNull CreatureMoveResult moveResult, @NotNull MoveRuleBuilder moveRuleBuilder) {}

    /**
     * Leap-specific move rule data selecting whether target contact deals damage.
     * */
    public static final class LeapMoveRuleBuilder extends MoveRuleBuilder {
        private final boolean requiresTargetContact;

        private LeapMoveRuleBuilder(boolean requiresTargetContact) {
            super("");
            this.requiresTargetContact = requiresTargetContact;
        }

        private LeapMoveRuleBuilder setLeapPriorityPredicate(int priority, @NotNull DetectionRule detectionRule) {
            this.setPriorityPredicate((creature, target) -> {
                if (target == null || !target.isEntityAlive()) return -1;
                if (!creature.isOnGround() || creature.bodyTouchingLiquid() || !creature.getNavigationBuilder().getCanLeap()) return -1;
                if (!target.onGround || target.isInWater()) return -1;
                if (!creature.canLeapToAttack() && !creature.atFrustrationThreshold()) return -1;
                return detectionRule.targetWithinRange(creature, target) ? priority : -1;
            });
            this.addDetectionRule(detectionRule);
            return this;
        }

        public boolean requiresTargetContact() {
            return this.requiresTargetContact;
        }
    }
}