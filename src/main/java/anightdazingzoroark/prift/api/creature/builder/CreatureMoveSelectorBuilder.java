package anightdazingzoroark.prift.api.creature.builder;

import anightdazingzoroark.prift.api.creature.CreatureMoveResult;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder.MoveCategory;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HitboxDistanceFromUserDetectionRule;
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

    public CreatureMoveSelectorBuilder setMoveRules(@NotNull List<MoveRule> rules) {
        this.checkIfLocked();
        rules.forEach(rule -> rule.moveRuleBuilder().lock());
        this.moveRules.clear();
        this.moveRules.addAll(rules);
        return this;
    }

    /**
     * Make it so that sprinting can be used as an attack by this creature.
     * Note that its only for when its on its own, when controlled by a rider
     * it can spring to attack when commanded to (by simply sprinting lol)
     * */
    public CreatureMoveSelectorBuilder setCanSprintToAttack(double minDist, double maxDist) {
        this.checkIfLocked();
        if (minDist > maxDist) throw new IllegalArgumentException(minDist+" is greater than "+maxDist+"!");

        MoveRuleBuilder sprintMoveRuleBuilder = new MoveRuleBuilder("", MoveCategory.MELEE)
                .setPriorityPredicate((creature, target) -> {
                    if (target == null || !target.isEntityAlive()) return false;
                    if (!creature.canSprintToAttack() && !creature.atFrustrationThreshold()) return false;
                    boolean outsideCreatureAABB = !creature.getEntityBoundingBox().grow(1e-5D).intersects(target.getEntityBoundingBox());
                    boolean outsideInnerBound = !creature.getEntityBoundingBox().grow(minDist).grow(1e-5D).intersects(target.getEntityBoundingBox());
                    boolean withinOuterBound = creature.getEntityBoundingBox().grow(maxDist).grow(1e-5D).intersects(target.getEntityBoundingBox());

                    if (!outsideCreatureAABB || !withinOuterBound || !outsideInnerBound) return false;
                    double distance = creature.horizontalDistanceFromEntity(target);
                    if (distance < minDist || distance > maxDist) return false;
                    return creature.hasStraightWalkingPathTo(target);
                })
                .addDetectionRule(new HitboxDistanceFromUserDetectionRule(4D))
                .setUseWhenFrustrated();
        sprintMoveRuleBuilder.lock();

        this.moveRules.add(new MoveRule(CreatureMoveResult.SPRINT, sprintMoveRuleBuilder));
        return this;
    }

    /**
     * Allow the AI to leap toward a target within the given distance range.
     * */
    public CreatureMoveSelectorBuilder setCanLeapToAttack(double minDist, double maxDist) {
        this.checkIfLocked();
        if (minDist > maxDist) throw new IllegalArgumentException(minDist+" is greater than "+maxDist+"!");

        MoveRuleBuilder leapMoveRuleBuilder = new MoveRuleBuilder("", MoveCategory.MELEE);
        leapMoveRuleBuilder.setPriorityPredicate((creature, target) -> {
            if (target == null || !target.isEntityAlive()) return false;
            if (!creature.isOnGround() || creature.bodyTouchingLiquid() || !creature.getNavigationBuilder().getCanLeap()) return false;
            if (!target.onGround || target.isInWater()) return false;
            double distance = creature.horizontalDistanceFromEntity(target);
            if (distance < minDist || distance > maxDist) return false;
            return creature.canLeapToAttack() || creature.atFrustrationThreshold();
        });
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

}
