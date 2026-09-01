package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * moves a creature, or the leader of its herd, to a reachable area away from
 * the position of the opponent its retreating from
 */
public class RiftRetreatFromCombat extends EntityAIBase {
    private static final int DESTINATION_SEARCH_ATTEMPTS = 12;
    private static final int MAXIMUM_RETREAT_TICKS = 1200;
    private static final int RETREAT_SEGMENT_DISTANCE = 12;
    private static final int[] RETREAT_DIST_RANGE = new int[]{32, 48};

    @NotNull
    private final RiftCreature creature;
    @Nullable
    private Path initialPath;
    private int retreatTicks;

    public RiftRetreatFromCombat(@NotNull RiftCreature creature) {
        this.creature = creature;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (!this.creature.isRetreating() || !this.creature.canLeadHerdBehavior()) return false;
        if (this.creature.bodyTouchingLiquid() && !this.creature.getNavigationBuilder().getCanSwim()) return false;

        Vec3d destination = this.creature.getRetreatDestination();
        if (destination != null) {
            this.initialPath = this.creature.getNavigator().getPathToXYZ(destination.x, destination.y, destination.z);
            if (this.initialPath != null) return true;
            this.creature.clearRetreatDestination();
        }

        this.initialPath = this.findRetreatPath();
        return this.initialPath != null;
    }

    @Nullable
    private Path findRetreatPath() {
        Vec3d threatPosition = this.creature.getRetreatThreatPosition();
        if (threatPosition == null) return null;

        Vec3d retreatStart = this.creature.getPositionVector();
        double bestThreatDistanceSq = threatPosition.squareDistanceTo(retreatStart);
        Path bestPath = null;
        Vec3d bestDestination = null;

        for (int attempt = 0; attempt < DESTINATION_SEARCH_ATTEMPTS; attempt++) {
            Vec3d candidate = RandomPositionGenerator.findRandomTargetBlockAwayFrom(
                    this.creature,
                    RETREAT_SEGMENT_DISTANCE,
                    Math.max(4, this.creature.getMaxFallHeight()),
                    threatPosition
            );
            if (candidate == null) continue;

            Path candidatePath = this.creature.getNavigator().getPathToXYZ(candidate.x, candidate.y, candidate.z);
            if (candidatePath == null || candidatePath.getCurrentPathLength() <= 0) continue;

            Vec3d reachedDestination = candidatePath.getVectorFromIndex(
                    this.creature, candidatePath.getCurrentPathLength() - 1
            );
            double reachedHorizontalX = reachedDestination.x - retreatStart.x;
            double reachedHorizontalZ = reachedDestination.z - retreatStart.z;
            double reachedDistanceSq = reachedHorizontalX * reachedHorizontalX + reachedHorizontalZ * reachedHorizontalZ;
            double reachedThreatDistanceSq = threatPosition.squareDistanceTo(reachedDestination);
            if (reachedDistanceSq < 16D || reachedThreatDistanceSq <= bestThreatDistanceSq) {
                continue;
            }

            bestThreatDistanceSq = reachedThreatDistanceSq;
            bestPath = candidatePath;
            bestDestination = reachedDestination;
        }

        if (bestPath == null) return null;

        this.creature.setRetreatDestination(bestDestination);
        return bestPath;
    }

    @Override
    public void startExecuting() {
        if (this.initialPath != null && !this.creature.getNavigator().setPath(this.initialPath, 1D)) {
            this.creature.clearRetreatDestination();
            this.initialPath = null;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.creature.isRetreating() && this.creature.canLeadHerdBehavior()
                && this.creature.getRetreatDestination() != null
                && (!this.creature.bodyTouchingLiquid() || this.creature.getNavigationBuilder().getCanSwim());
    }

    @Override
    public void updateTask() {
        this.retreatTicks++;
        Vec3d destination = this.creature.getRetreatDestination();
        if (destination == null) {
            this.creature.getNavigator().clearPath();
            return;
        }

        double arrivalDistance = Math.max(2D, this.creature.width * 0.5D);
        if (this.creature.getDistanceSq(destination.x, destination.y, destination.z) <= arrivalDistance * arrivalDistance) {
            this.creature.getNavigator().clearPath();
            Vec3d retreatOrigin = this.creature.getRetreatOriginPosition();
            double horizontalX = retreatOrigin == null ? 0D : this.creature.posX - retreatOrigin.x;
            double horizontalZ = retreatOrigin == null ? 0D : this.creature.posZ - retreatOrigin.z;
            double minimumRetreatDistanceSq = RETREAT_DIST_RANGE[0] * RETREAT_DIST_RANGE[0];
            if (retreatOrigin != null && horizontalX * horizontalX + horizontalZ * horizontalZ >= minimumRetreatDistanceSq) {
                //the leader intentionally waits here until every herd member catches up
                if (this.creature.isRetreatGroupAssembled()) this.creature.finishRetreat();
            }
            else this.startNewRetreatSegment();
            return;
        }

        if (this.retreatTicks >= MAXIMUM_RETREAT_TICKS) {
            this.startNewRetreatSegment();
            return;
        }

        //the destination is static, so replacing an active path only causes needless stops
        if (!this.creature.getNavigator().noPath()) return;

        Path replacementPath = this.creature.getNavigator().getPathToXYZ(destination.x, destination.y, destination.z);
        if (replacementPath != null && this.creature.getNavigator().setPath(replacementPath, 1D)) {
            return;
        }

        this.startNewRetreatSegment();
    }

    private void startNewRetreatSegment() {
        this.creature.getNavigator().clearPath();
        this.creature.clearRetreatDestination();
        this.initialPath = this.findRetreatPath();
        this.retreatTicks = 0;
        if (this.initialPath != null && !this.creature.getNavigator().setPath(this.initialPath, 1D)) {
            this.creature.clearRetreatDestination();
            this.initialPath = null;
        }
    }

    @Override
    public void resetTask() {
        this.creature.getNavigator().clearPath();
        this.initialPath = null;
        if (!this.creature.isRetreating()) {
            this.retreatTicks = 0;
            //for gameplay purposes, reset stamina after finish retreat
            this.creature.setStamina(this.creature.getMaxStamina());
        }
    }
}
