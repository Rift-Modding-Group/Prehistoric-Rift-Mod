package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.ChunkCache;

import org.jetbrains.annotations.Nullable;

/**
 * Ground navigator for creatures for this mod
 * */
public class RiftCreaturePathNavigate extends PathNavigateGround {
    private static final int WALKING_PATH_CACHE_TICKS = 10;
    private static final double MAXIMUM_STRAIGHT_PATH_DEVIATION_SQUARED = 1D;
    private static final double DIRECT_SUPPORT_SCAN_STEP = 0.25D;
    private static final double DIRECT_SUPPORT_PROBE_RADIUS = 0.05D;
    private static final double DIRECT_SUPPORT_VERTICAL_RANGE = RiftCreatureMoveHelper.STANDARD_JUMP_HEIGHT + 0.125D;

    private RiftCreatureWalkNodeProcessor riftNodeProcessor;
    private final RiftCreature creature;
    @Nullable
    private BlockPos cachedWalkingPathOrigin;
    @Nullable
    private BlockPos cachedWalkingPathTarget;
    private long cachedWalkingPathTime = Long.MIN_VALUE;
    private boolean cachedWalkingPathResult;
    private boolean cachedSafeDownwardWalkingPathResult;

    public RiftCreaturePathNavigate(RiftCreature creature, World world) {
        super(creature, world);
        this.creature = creature;
        this.setCanSwim(true);
    }

    @Override
    protected PathFinder getPathFinder() {
        this.riftNodeProcessor = new RiftCreatureWalkNodeProcessor((RiftCreature) this.entity, true);
        this.nodeProcessor = this.riftNodeProcessor;
        this.nodeProcessor.setCanEnterDoors(true);
        return new PathFinder(this.nodeProcessor);
    }

    @Override
    @Nullable
    public Path getPathToEntityLiving(Entity target) {
        this.riftNodeProcessor.setWaterPathingAllowed(true);
        Path path = super.getPathToEntityLiving(target);
        this.riftNodeProcessor.setWaterPathingAllowed(false);
        return path;
    }

    public boolean tryMoveToPositionUsingWater(BlockPos target, double speed) {
        this.riftNodeProcessor.setWaterPathingAllowed(true);
        Path path = super.getPathToPos(target);
        this.riftNodeProcessor.setWaterPathingAllowed(false);
        return path != null && this.setPath(path, speed);
    }

    @Override
    protected boolean canNavigate() {
        return this.creature.getNavigationBuilder().getCanWalk() && (super.canNavigate() || this.creature.bodyTouchingLiquid());
    }

    @Override
    protected Vec3d getEntityPosition() {
        if (this.creature.bodyTouchingLiquid()) {
            return new Vec3d(this.creature.posX, this.creature.posY, this.creature.posZ);
        }
        return super.getEntityPosition();
    }

    @Override
    protected void pathFollow() {
        if (!this.creature.bodyTouchingLiquid() && !this.isFollowingWaterPath()) {
            super.pathFollow();
            return;
        }

        Vec3d creaturePosition = this.getEntityPosition();
        this.maxDistanceToWaypoint = Math.max(0.75F, this.creature.width * 0.5F);
        double waypointDistanceSq = this.maxDistanceToWaypoint * this.maxDistanceToWaypoint;

        while (this.currentPath != null && !this.currentPath.isFinished()) {
            Vec3d waypoint = this.currentPath.getVectorFromIndex(
                    this.creature,
                    this.currentPath.getCurrentPathIndex()
            );
            double displacementX = waypoint.x - this.creature.posX;
            double displacementZ = waypoint.z - this.creature.posZ;
            if (displacementX * displacementX + displacementZ * displacementZ > waypointDistanceSq) break;
            this.currentPath.incrementPathIndex();
        }

        this.checkForStuck(creaturePosition);
    }

    public boolean isFollowingWaterPath() {
        if (this.currentPath == null || this.currentPath.isFinished()) return false;
        PathPoint currentPoint = this.currentPath.getPathPointFromIndex(this.currentPath.getCurrentPathIndex());
        return currentPoint.nodeType == PathNodeType.WATER;
    }

    @Override
    public boolean tryMoveToEntityLiving(Entity target, double speed) {
        Path path = this.getPathToEntityLiving(target);
        boolean startedPath = path != null && this.setPath(path, speed);
        if (target == this.creature.getAttackTarget()) {
            this.creature.setUnableToPathToTarget(!startedPath || !this.pathReachesSafeTarget(path, target));
        }
        return startedPath;
    }

    public boolean tryMoveToEntityLivingUsingWater(Entity target, double speed) {
        Path path = this.getPathToEntityLiving(target);
        if (path == null || !this.pathUsesWater(path)) return false;

        boolean startedPath = this.setPath(path, speed);
        if (target == this.creature.getAttackTarget()) {
            this.creature.setUnableToPathToTarget(!startedPath);
        }
        return startedPath;
    }

    private boolean pathUsesWater(Path path) {
        for (int index = 0; index < path.getCurrentPathLength(); index++) {
            if (path.getPathPointFromIndex(index).nodeType == PathNodeType.WATER) return true;
        }
        return false;
    }

    private boolean pathReachesSafeTarget(@Nullable Path path, Entity target) {
        return path != null && this.pathReachesTarget(path, target) && this.pathIsSafe(path);
    }

    private boolean pathReachesTarget(@Nullable Path path, Entity target) {
        PathPoint finalPoint = path == null ? null : path.getFinalPathPoint();
        if (finalPoint == null) return false;

        int nodeSize = MathHelper.floor(this.creature.width + 1F);
        return finalPoint.x == MathHelper.floor(target.posX - nodeSize * 0.5D)
                && finalPoint.y == MathHelper.floor(target.getEntityBoundingBox().minY)
                && finalPoint.z == MathHelper.floor(target.posZ - nodeSize * 0.5D);
    }

    private boolean pathIsSafe(Path path) {
        for (int index = 0; index < path.getCurrentPathLength(); index++) {
            PathNodeType nodeType = path.getPathPointFromIndex(index).nodeType;
            if (nodeType == PathNodeType.DANGER_FIRE
                    || nodeType == PathNodeType.DAMAGE_FIRE
                    || nodeType == PathNodeType.DANGER_CACTUS
                    || nodeType == PathNodeType.DAMAGE_CACTUS
                    || nodeType == PathNodeType.DANGER_OTHER
                    || nodeType == PathNodeType.DAMAGE_OTHER) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether the current walking waypoint is immediately followed by a survivable
     * lower waypoint. The move helper uses this to avoid treating the ledge between them as a
     * blocked gap before the creature can reach the downward part of its path.
     */
    boolean isApproachingSafeDownwardPathTransition() {
        if (this.currentPath == null || this.currentPath.isFinished()) return false;

        int currentIndex = this.currentPath.getCurrentPathIndex();
        int nextIndex = currentIndex + 1;
        if (nextIndex >= this.currentPath.getCurrentPathLength()) return false;

        PathPoint currentPoint = this.currentPath.getPathPointFromIndex(currentIndex);
        PathPoint nextPoint = this.currentPath.getPathPointFromIndex(nextIndex);
        int downwardDistance = currentPoint.y - nextPoint.y;
        return downwardDistance > 0
                && downwardDistance <= this.creature.getMaxFallHeight()
                && nextPoint.nodeType == PathNodeType.WALKABLE;
    }

    public boolean hasStraightWalkingPathTo(Entity target) {
        this.updateWalkingPathCache(target);
        return this.cachedWalkingPathResult;
    }

    public boolean hasSafeDownwardWalkingPathTo(Entity target) {
        double creatureY = this.creature.getEntityBoundingBox().minY;
        double targetY = target.getEntityBoundingBox().minY;
        if (MathHelper.floor(creatureY + 1E-3D) <= MathHelper.floor(targetY + 1E-3D)
                || creatureY - targetY > this.creature.getMaxFallHeight() + 1E-3D) {
            return false;
        }

        this.updateWalkingPathCache(target);
        return this.cachedSafeDownwardWalkingPathResult;
    }

    private void updateWalkingPathCache(Entity target) {
        BlockPos origin = new BlockPos(this.creature);
        BlockPos destination = new BlockPos(target);
        long worldTime = this.world.getTotalWorldTime();
        boolean cacheExpired = worldTime - this.cachedWalkingPathTime >= WALKING_PATH_CACHE_TICKS;
        if (cacheExpired || !origin.equals(this.cachedWalkingPathOrigin) || !destination.equals(this.cachedWalkingPathTarget)) {
            this.cachedWalkingPathOrigin = origin;
            this.cachedWalkingPathTarget = destination;
            this.cachedWalkingPathTime = worldTime;
            this.updateWalkingPathResults(target);
        }
    }

    private void updateWalkingPathResults(Entity target) {
        this.cachedWalkingPathResult = false;
        this.cachedSafeDownwardWalkingPathResult = false;
        if (!this.canNavigate()) return;

        RiftCreatureWalkNodeProcessor walkingProcessor = new RiftCreatureWalkNodeProcessor(this.creature, false);
        walkingProcessor.setCanEnterDoors(this.nodeProcessor.getCanEnterDoors());
        walkingProcessor.setCanOpenDoors(this.nodeProcessor.getCanOpenDoors());
        walkingProcessor.setCanSwim(this.nodeProcessor.getCanSwim());
        PathFinder walkingPathFinder = new PathFinder(walkingProcessor);
        float searchRange = this.getPathSearchRange();
        BlockPos cacheCenter = new BlockPos(this.creature).up();
        int cacheRadius = (int)(searchRange + 16F);
        ChunkCache chunkCache = new ChunkCache(
                this.world,
                cacheCenter.add(-cacheRadius, -cacheRadius, -cacheRadius),
                cacheCenter.add(cacheRadius, cacheRadius, cacheRadius),
                0
        );
        Path path = walkingPathFinder.findPath(chunkCache, this.creature, target, searchRange);
        if (!this.pathReachesTarget(path, target)) return;

        //a partial walking path ending at the same height can merely be the near edge of a gap
        //only a path that reaches the target node proves that walking down is possible
        this.cachedSafeDownwardWalkingPathResult = this.pathIsSafe(path);
        this.cachedWalkingPathResult = this.pathIsStraight(path) && this.directRouteHasSupport(target);
    }

    private boolean pathIsStraight(Path path) {
        if (path.getCurrentPathLength() <= 1) return true;

        PathPoint start = path.getPathPointFromIndex(0);
        PathPoint end = path.getFinalPathPoint();
        if (end == null) return false;
        double directionX = end.x - start.x;
        double directionZ = end.z - start.z;
        double lengthSq = directionX * directionX + directionZ * directionZ;
        if (lengthSq < 1E-6D) return true;

        double lastProgress = 0D;
        for (int index = 1; index < path.getCurrentPathLength(); index++) {
            PathPoint point = path.getPathPointFromIndex(index);
            double pointX = point.x - start.x;
            double pointZ = point.z - start.z;
            double progress = (pointX * directionX + pointZ * directionZ) / lengthSq;
            if (progress + 1E-6D < lastProgress) return false;

            double closestX = directionX * Math.clamp(progress, 0D, 1D);
            double closestZ = directionZ * Math.clamp(progress, 0D, 1D);
            double deviationX = pointX - closestX;
            double deviationZ = pointZ - closestZ;
            if (deviationX * deviationX + deviationZ * deviationZ > MAXIMUM_STRAIGHT_PATH_DEVIATION_SQUARED) {
                return false;
            }
            lastProgress = progress;
        }
        return true;
    }

    private boolean directRouteHasSupport(Entity target) {
        double displacementX = target.posX - this.creature.posX;
        double displacementY = target.posY - this.creature.getEntityBoundingBox().minY;
        double displacementZ = target.posZ - this.creature.posZ;
        double horizontalDistance = Math.sqrt(displacementX * displacementX + displacementZ * displacementZ);
        int samples = Math.max(1, (int)Math.ceil(horizontalDistance / DIRECT_SUPPORT_SCAN_STEP));

        for (int sample = 1; sample <= samples; sample++) {
            double progress = (double)sample / samples;
            double sampleX = this.creature.posX + displacementX * progress;
            double sampleY = this.creature.getEntityBoundingBox().minY + displacementY * progress;
            double sampleZ = this.creature.posZ + displacementZ * progress;
            AxisAlignedBB supportProbe = new AxisAlignedBB(
                    sampleX - DIRECT_SUPPORT_PROBE_RADIUS,
                    sampleY - DIRECT_SUPPORT_VERTICAL_RANGE,
                    sampleZ - DIRECT_SUPPORT_PROBE_RADIUS,
                    sampleX + DIRECT_SUPPORT_PROBE_RADIUS,
                    sampleY + DIRECT_SUPPORT_VERTICAL_RANGE,
                    sampleZ + DIRECT_SUPPORT_PROBE_RADIUS
            );
            if (!this.world.collidesWithAnyBlock(supportProbe)) return false;
        }
        return true;
    }
}
