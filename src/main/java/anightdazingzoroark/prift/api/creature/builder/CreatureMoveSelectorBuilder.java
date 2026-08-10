package anightdazingzoroark.prift.api.creature.builder;

import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.CreatureMoveResult;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Creature-level AI policy for choosing moves.
 * */
//todo: add move combos
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

        this.moveRules.add(new MoveRule(CreatureMoveResult.SPRINT, new MoveRuleBuilder("")
                .setPriorityPredicate((creature, target) -> {
                    if (target == null || !target.isEntityAlive()) return -1;
                    if (creature.getSprintToAttackCooldown() > 0 && !creature.atFrustrationThreshold()) return -1;
                    boolean outsideCreatureAABB = !creature.getEntityBoundingBox().grow(1e-5D).intersects(target.getEntityBoundingBox());
                    boolean outsideInnerBound = !creature.getEntityBoundingBox().grow(minDist).grow(1e-5D).intersects(target.getEntityBoundingBox());
                    boolean withinOuterBound = creature.getEntityBoundingBox().grow(maxDist).grow(1e-5D).intersects(target.getEntityBoundingBox());

                    if (!outsideCreatureAABB || !withinOuterBound || !outsideInnerBound) return -1;
                    return creature.hasStraightWalkingPathTo(target) ? priority : -1;
                })
                .setDetectionRule(new DistanceFromUserDetectionRule("", minDist, maxDist))
                .setUseWhenFrustrated())
        );
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

        DistanceFromUserDetectionRule detectionRule = new DistanceFromUserDetectionRule("", minDist, maxDist);
        LeapMoveRuleBuilder leapMoveRuleBuilder = new LeapMoveRuleBuilder(requiresTargetContact).setLeapPriorityPredicate(priority, detectionRule);
        leapMoveRuleBuilder.setUseWhenFrustrated();
        leapMoveRuleBuilder.lock();

        this.moveRules.add(new MoveRule(CreatureMoveResult.LEAP, leapMoveRuleBuilder));
        return this;
    }

    @NotNull
    public List<MoveRule> getMoveRules() {
        return this.moveRules;
    }

    /**
     * Put this on every setter in builder to protect from post-creation editing
     * */
    protected void checkIfLocked() {
        if (this.locked) throw new IllegalCallerException("A setter for a move selector builder cannot be called after the move selector is registered!");
    }

    public record MoveRule(@NotNull CreatureMoveResult moveResult, @NotNull MoveRuleBuilder moveRuleBuilder) {
        @Override
        public boolean equals(Object object) {
            if (!(object instanceof MoveRule(CreatureMoveResult otherResult, MoveRuleBuilder otherMoveRuleBuilder))) return false;

            if (otherResult != CreatureMoveResult.USE_MOVE) return otherResult == this.moveResult;
            else return otherMoveRuleBuilder.getMoveName().equals(this.moveRuleBuilder.getMoveName());
        }
    }

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
                if (creature.getLeapToAttackCooldown() > 0 && !creature.atFrustrationThreshold()) return -1;
                return detectionRule.targetWithinRange(creature, target) ? priority : -1;
            });
            this.setDetectionRule(detectionRule);
            return this;
        }

        public boolean requiresTargetContact() {
            return this.requiresTargetContact;
        }
    }

    //-----target detection rules for moves-----
    public abstract static class DetectionRule {
        public abstract boolean targetWithinRange(@NotNull ICreature user, @NotNull EntityLivingBase target);
    }

    public static class BoundingBoxDetectionRule extends DetectionRule {
        @NotNull
        private final String boundingBoxName;

        public BoundingBoxDetectionRule(@NotNull String boundingBoxName) {
            this.boundingBoxName = boundingBoxName;
        }

        @Override
        public boolean targetWithinRange(@NotNull ICreature user, @NotNull EntityLivingBase target) {
            return user.aabbIntersectsBoundingBox(target.getEntityBoundingBox(), this.boundingBoxName);
        }
    }

    public static class DistanceFromUserDetectionRule extends DetectionRule {
        @NotNull
        private final String locatorName;
        private final double minDistance;
        private final double maxDistance;

        public DistanceFromUserDetectionRule(double maxDistance) {
            this("", -1, maxDistance);
        }

        public DistanceFromUserDetectionRule(double minDistance, double maxDistance) {
            this("", minDistance, maxDistance);
        }

        public DistanceFromUserDetectionRule(@NotNull String locatorName, double maxDistance) {
            this(locatorName, -1, maxDistance);
        }

        public DistanceFromUserDetectionRule(@NotNull String locatorName, double minDistance, double maxDistance) {
            this.locatorName = locatorName;
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;
        }

        @Override
        public boolean targetWithinRange(@NotNull ICreature user, @NotNull EntityLivingBase target) {
            boolean outsideCreatureAABB = this.locatorName.isEmpty() || !this.withinCreatureAABB(user, target);

            //if minDistance is negative, it means only maxDistance matters
            if (this.minDistance < 0) {
                boolean withinBoundOfLocator = this.aabbFromLocator(user, this.maxDistance).grow(1e-5D).intersects(target.getEntityBoundingBox());
                return withinBoundOfLocator && outsideCreatureAABB;
            }
            else if (this.maxDistance >= this.minDistance) {
                boolean withinOuterBound = this.aabbFromLocator(user, this.maxDistance).grow(1e-5D).intersects(target.getEntityBoundingBox());
                boolean outsideInnerBound = !this.aabbFromLocator(user, this.minDistance).grow(1e-5D).intersects(target.getEntityBoundingBox());
                return withinOuterBound && outsideInnerBound && outsideCreatureAABB;
            }
            throw new IllegalArgumentException("Given maxDistance is smaller than minDistance!");
        }

        @NotNull
        private AxisAlignedBB aabbFromLocator(@NotNull ICreature creature, double width) {
            if (this.locatorName.isEmpty()) {
                AxisAlignedBB creatureAABB = creature.getEntityBoundingBox();
                return creatureAABB.grow(width);
            }
            else {
                Vec3d worldSpaceLocator = creature.getLocatorWorldPos(this.locatorName);
                return new AxisAlignedBB(
                        worldSpaceLocator.x - width / 2D,
                        worldSpaceLocator.y - width / 2D,
                        worldSpaceLocator.z - width / 2D,
                        worldSpaceLocator.x + width / 2D,
                        worldSpaceLocator.y + width / 2D,
                        worldSpaceLocator.z + width / 2D
                );
            }
        }

        private boolean withinCreatureAABB(@NotNull ICreature creature, EntityLivingBase target) {
            return creature.getEntityBoundingBox().grow(1e-5D).intersects(target.getEntityBoundingBox());
        }
    }
}
