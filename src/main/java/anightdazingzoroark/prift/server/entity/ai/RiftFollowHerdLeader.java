package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureHerdHelper;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RiftFollowHerdLeader extends EntityAIBase {
    private static final int PATH_RECALCULATION_INTERVAL = 10;
    private static final double MINIMUM_CATCH_UP_DISTANCE = 12D;
    private static final double MAXIMUM_SLOT_HEIGHT_DIFFERENCE = 1D;

    @NotNull
    private final RiftCreature creature;
    @Nullable
    private RiftCreature leader;
    @Nullable
    private RiftCreature pathLeader;
    @Nullable
    private Vec3d pathDestination;
    private int pathRecalculationCountdown;
    private boolean catchingUp;

    public RiftFollowHerdLeader(@NotNull RiftCreature creature) {
        this.creature = creature;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        RiftCreature currentLeader = this.creature.getHerdLeader();
        if (this.pathLeader != currentLeader || currentLeader == null || !currentLeader.isEntityAlive()) {
            this.pathLeader = null;
            this.pathDestination = null;
        }
        if (this.creature.getAttackTarget() != null || currentLeader == null || currentLeader == this.creature || !currentLeader.isEntityAlive()) {
            return false;
        }
        Vec3d currentFollowPosition = this.getFollowPosition();
        if (currentFollowPosition == null) return false;

        double startDistance = this.getStopDistance() + Math.max(1D, this.creature.width * 0.5D);
        if (this.isWithinFollowPosition(currentLeader, currentFollowPosition, startDistance)) return false;

        this.leader = currentLeader;
        return true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        RiftCreature currentLeader = this.creature.getHerdLeader();
        Vec3d currentFollowPosition = this.getFollowPosition();
        double stopDistance = this.getStopDistance();
        return currentLeader != null && currentLeader == this.leader
                && currentLeader.isEntityAlive() && this.creature.getAttackTarget() == null
                && currentFollowPosition != null && !this.isWithinFollowPosition(currentLeader, currentFollowPosition, stopDistance);
    }

    @Override
    public void startExecuting() {
        this.pathRecalculationCountdown = 0;
    }

    @Override
    public void resetTask() {
        RiftCreature currentLeader = this.creature.getHerdLeader();
        Vec3d currentFollowPosition = this.getFollowPosition();
        boolean preservePathDestination = currentLeader != null && currentLeader == this.leader
                && this.creature.getAttackTarget() == null && currentFollowPosition != null
                && this.isWithinFollowPosition(currentLeader, currentFollowPosition, this.getStopDistance());
        this.creature.getNavigator().clearPath();
        this.leader = null;
        this.catchingUp = false;
        if (!preservePathDestination) {
            this.pathLeader = null;
            this.pathDestination = null;
        }
    }

    @Override
    public void updateTask() {
        if (this.leader == null) return;
        Vec3d currentFollowPosition = this.getFollowPosition();
        if (currentFollowPosition == null) return;

        double formationDistance = Math.sqrt(this.leader.getDistanceSq(currentFollowPosition.x, currentFollowPosition.y, currentFollowPosition.z));
        double catchUpDistance = Math.max(MINIMUM_CATCH_UP_DISTANCE, formationDistance + 2D);
        boolean shouldCatchUp = this.creature.getDistanceSq(this.leader) > catchUpDistance * catchUpDistance;
        if (shouldCatchUp != this.catchingUp) {
            this.catchingUp = shouldCatchUp;
            this.creature.getNavigator().clearPath();
            this.pathRecalculationCountdown = 0;
        }
        if (--this.pathRecalculationCountdown > 0) return;

        this.pathRecalculationCountdown = PATH_RECALCULATION_INTERVAL;
        if (this.catchingUp) {
            this.pathLeader = null;
            this.pathDestination = null;
            boolean startedCatchUpPath;
            if (this.leader.bodyTouchingLiquid()) {
                startedCatchUpPath = this.creature.getCreaturePathNavigate().tryMoveToEntityLivingUsingWater(this.leader, 1D);
            }
            else startedCatchUpPath = this.creature.getNavigator().tryMoveToEntityLiving(this.leader, 1D);
            if (startedCatchUpPath) return;

            this.creature.getNavigator().clearPath();
        }

        if (this.tryMoveToPosition(currentFollowPosition)) return;
        this.creature.getNavigator().clearPath();
    }

    @Nullable
    private Vec3d getFollowPosition() {
        RiftCreatureHerdHelper herd = this.creature.getHerd();
        return herd == null ? null : herd.getFollowPosition(this.creature);
    }

    private double getStopDistance() {
        return Math.max(1D, this.creature.width * 0.5D);
    }

    private boolean isWithinFollowPosition(@NotNull RiftCreature currentLeader, @NotNull Vec3d position, double horizontalDistance) {
        double displacementX = this.creature.posX - position.x;
        double displacementZ = this.creature.posZ - position.z;
        double destinationY = position.y;
        if (this.pathLeader == currentLeader && this.pathDestination != null
                && Math.floor(position.x) == Math.floor(this.pathDestination.x)
                && Math.floor(position.z) == Math.floor(this.pathDestination.z)
        ) {
            destinationY = this.pathDestination.y;
        }
        return displacementX * displacementX + displacementZ * displacementZ <= horizontalDistance * horizontalDistance
                && (this.creature.bodyTouchingLiquid() || Math.abs(this.creature.posY - destinationY) <= MAXIMUM_SLOT_HEIGHT_DIFFERENCE);
    }

    private boolean tryMoveToPosition(@NotNull Vec3d position) {
        if (this.leader == null) return false;

        Vec3d reachedEndpoint = null;
        if (this.leader.bodyTouchingLiquid() || this.creature.bodyTouchingLiquid()) {
            if (this.creature.getCreaturePathNavigate().tryMoveToPositionUsingWater(new BlockPos(position), 1D)) {
                reachedEndpoint = this.getReachedPathEndpoint(position);
            }
        }
        else {
            if (this.creature.getNavigator().tryMoveToXYZ(position.x, position.y, position.z, 1D)) {
                reachedEndpoint = this.getReachedPathEndpoint(position);
            }
            if (reachedEndpoint == null) {
                this.creature.getNavigator().clearPath();
                if (this.creature.getCreaturePathNavigate().tryMoveToPositionUsingWater(new BlockPos(position), 1)) {
                    reachedEndpoint = this.getReachedPathEndpoint(position);
                }
            }
        }
        if (reachedEndpoint == null) {
            this.creature.getNavigator().clearPath();
            this.pathLeader = null;
            this.pathDestination = null;
            return false;
        }

        this.pathLeader = this.leader;
        this.pathDestination = new Vec3d(position.x, reachedEndpoint.y, position.z);
        return true;
    }

    @Nullable
    private Vec3d getReachedPathEndpoint(@NotNull Vec3d position) {
        Path path = this.creature.getNavigator().getPath();
        if (path == null || path.getCurrentPathLength() <= 0) return null;

        PathPoint finalPoint = path.getFinalPathPoint();
        if (finalPoint == null) return null;

        BlockPos targetPosition = new BlockPos(position);
        int nodeSize = MathHelper.floor(this.creature.width + 1f);
        int expectedX = MathHelper.floor(targetPosition.getX() + 0.5D - nodeSize * 0.5D);
        int expectedZ = MathHelper.floor(targetPosition.getZ() + 0.5D - nodeSize * 0.5D);
        return finalPoint.x == expectedX && finalPoint.z == expectedZ
                ? path.getVectorFromIndex(this.creature, path.getCurrentPathLength() - 1) : null;
    }
}
