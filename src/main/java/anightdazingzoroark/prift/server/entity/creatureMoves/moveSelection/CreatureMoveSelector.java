package anightdazingzoroark.prift.server.entity.creatureMoves.moveSelection;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
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
public class CreatureMoveSelector {
    //general storage of move rules
    private final List<MoveRule> moveRules = new ArrayList<>();

    public CreatureMoveSelector setMoveRule(MoveRuleBuilder moveRuleBuilder) {
        this.moveRules.add(new MoveRule(MoveResult.USE_MOVE, moveRuleBuilder));
        return this;
    }

    /**
     * Make it so that sprinting can be used as an attack by this creature.
     * Note that its only for when its on its own, when controlled by a rider
     * it can spring to attack when commanded to (by simply sprinting lol)
     * */
    public CreatureMoveSelector setCanSprintToAttack() {
        this.moveRules.add(new MoveRule(MoveResult.SPRINT, new MoveRuleBuilder("")
                .setPriorityPredicate((creature, target) -> {
                    if (target == null) return -1;
                    double distFromTarget = creature.getDistance(target);
                    double minReach = creature.width + 3; //is temporary
                    boolean canSprintNow = creature.sprintToAttackCooldown <= 0 || creature.atFrustrationThreshold();
                    boolean sprintCondition = distFromTarget <= 16D && distFromTarget >= minReach && canSprintNow;
                    return sprintCondition ? (creature.atFrustrationThreshold() ? 0 : 1) : -1;
                })
                .setDetectionRule(new DistanceFromUserDetectionRule("", 8D,16D)))
        );
        return this;
    }

    public CreatureMoveSelector setCanLeapToAttack() {
        this.moveRules.add(new MoveRule(MoveResult.LEAP, new MoveRuleBuilder("")
                .setDetectionRule(new DistanceFromUserDetectionRule("", 8D,16D)))
        );
        return this;
    }

    public List<MoveRule> getMoveRules() {
        return this.moveRules;
    }

    public enum MoveResult {
        USE_MOVE,
        USE_MOVE_COMBO,
        SPRINT,
        LEAP
    }

    public record MoveRule(@NotNull MoveResult moveResult, @NotNull MoveRuleBuilder moveRuleBuilder) {
        @Override
        public boolean equals(Object object) {
            if (!(object instanceof MoveRule(MoveResult otherResult, MoveRuleBuilder otherMoveRuleBuilder))) return false;

            if (otherResult != MoveResult.USE_MOVE) return otherResult == this.moveResult;
            else return otherMoveRuleBuilder.getMoveName().equals(this.moveRuleBuilder.getMoveName());
        }
    }

    //-----target detection rules for moves-----
    public abstract static class DetectionRule {
        public abstract boolean targetWithinRange(@NotNull RiftCreature user, @NotNull EntityLivingBase target);
    }

    public static class BoundingBoxDetectionRule extends DetectionRule {
        @NotNull
        private final String boundingBoxName;

        public BoundingBoxDetectionRule(@NotNull String boundingBoxName) {
            this.boundingBoxName = boundingBoxName;
        }

        @Override
        public boolean targetWithinRange(@NotNull RiftCreature user, @NotNull EntityLivingBase target) {
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
        public boolean targetWithinRange(@NotNull RiftCreature user, @NotNull EntityLivingBase target) {
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
            throw new UnsupportedOperationException("Given maxDistance is smaller than minDistance!");
        }

        @NotNull
        private AxisAlignedBB aabbFromLocator(@NotNull RiftCreature creature, double width) {
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

        private boolean withinCreatureAABB(@NotNull RiftCreature creature, EntityLivingBase target) {
            return creature.getEntityBoundingBox().grow(1e-5D).intersects(target.getEntityBoundingBox());
        }
    }
}