package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.lwjglx.util.vector.Quaternion;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Creature-level AI policy for choosing moves.
 * */
//todo: add move combos
public class CreatureMoveSelector {
    //general storage of move rules
    private final Map<MoveRule, BiFunction<RiftCreature, EntityLivingBase, Integer>> moveRules = new HashMap<>();

    public CreatureMoveSelector setMoveRule(String name, BiFunction<RiftCreature, EntityLivingBase, Integer> priorityPredicate) {
        return this.setMoveRule(name, priorityPredicate, new DistanceFromUserDetectionRule("", 8D));
    }

    public CreatureMoveSelector setMoveRule(
            String name,
            BiFunction<RiftCreature, EntityLivingBase, Integer> priorityPredicate,
            DetectionRule detectionRule
    ) {
        this.moveRules.put(new MoveRule(MoveResult.USE_MOVE, name, detectionRule), priorityPredicate);
        return this;
    }

    /**
     * Make it so that sprinting can be used as an attack by this creature.
     * Note that its only for when its on its own, when controlled by a rider
     * it can spring to attack when commanded to (by simply sprinting lol)
     * */
    public CreatureMoveSelector setCanSprintToAttack() {
        this.moveRules.put(new MoveRule(MoveResult.SPRINT, "", new DistanceFromUserDetectionRule("", 8D,16D)), (creature, target) -> {
            if (target == null) return -1;
            double distFromTarget = creature.getDistance(target);
            double minReach = creature.width + 3; //is temporary
            boolean sprintCondition = distFromTarget <= 16D && distFromTarget >= minReach && creature.sprintToAttackCooldown <= 0;
            return sprintCondition ? 1 : -1;
        });
        return this;
    }

    public CreatureMoveSelector setCanLeapToAttack(BiFunction<RiftCreature, EntityLivingBase, Integer> predicate) {
        this.moveRules.put(new MoveRule(MoveResult.LEAP, "", new DistanceFromUserDetectionRule("", 8D,16D)), predicate);
        return this;
    }

    public Map<MoveRule, BiFunction<RiftCreature, EntityLivingBase, Integer>> getMoveRules() {
        return this.moveRules;
    }

    public enum MoveResult {
        USE_MOVE,
        USE_MOVE_COMBO,
        SPRINT,
        LEAP
    }

    public record MoveRule(@NotNull MoveResult moveResult, @NotNull String name, @NotNull DetectionRule detectionRule) {
        @Override
        public boolean equals(Object object) {
            if (!(object instanceof MoveRule(MoveResult otherResult, String otherName, DetectionRule detectionRule))) return false;

            if (otherResult != MoveResult.USE_MOVE) return otherResult == this.moveResult;
            else return otherName.equals(this.name);
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
            //---check if the posVec is in range---
            //if minDistance is negative, it means only maxDistance matters
            double distance = this.getDistanceFromUserPoint(user, target);
            if (this.minDistance < 0) return distance <= this.maxDistance;
            else if (this.maxDistance >= this.minDistance) {
                return distance >= this.minDistance && distance <= this.maxDistance;
            }
            throw new UnsupportedOperationException("Given maxDistance is smaller than minDistance!");
        }

        private double getDistanceFromUserPoint(@NotNull RiftCreature user, @NotNull EntityLivingBase target) {
            return this.getUserPoint(user).distanceTo(target.getPositionVector());
        }

        @NotNull
        private Vec3d getUserPoint(@NotNull RiftCreature user) {
            //turn locator into world space position
            //note that if there's no locator, the creature's own position will be used instead
            AnimatedLocator animatedLocator = user.getAnimationData().getAnimatedLocator(this.locatorName);
            if (animatedLocator == null) return user.getPositionVector();

            Vec3d modelSpacePos = animatedLocator.getModelSpacePosition();
            float parentScale = user.scale();
            float locatorX = -(float) (modelSpacePos.x / 16f);
            float locatorY = (float) (modelSpacePos.y / 16f);
            float locatorZ = -(float) (modelSpacePos.z / 16f);
            Vec3d locatorPos = new Vec3d(locatorX * parentScale, locatorY * parentScale, locatorZ * parentScale);

            double yawHead = -Math.toRadians(user.rotationYawHead);
            double yawBody = -Math.toRadians(user.rotationYaw);
            double yaw = user.isBeingRidden() ? yawBody : yawHead;
            Quaternion quaternion = QuaternionUtils.createXYZQuaternion(0f, yaw, 0f);
            locatorPos = VectorUtils.rotateVectorWithQuaternion(locatorPos, quaternion);

            return new Vec3d(
                    user.posX + locatorPos.x,
                    user.posY + locatorPos.y,
                    user.posZ + locatorPos.z
            );
        }
    }
}
