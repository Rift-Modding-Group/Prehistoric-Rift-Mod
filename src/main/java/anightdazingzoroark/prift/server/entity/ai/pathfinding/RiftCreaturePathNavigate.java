package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.ChunkCache;

import org.jetbrains.annotations.Nullable;

/**
 * Ground navigator for creatures for this mod
 * */
public class RiftCreaturePathNavigate extends PathNavigateGround {
    private static final int WALKING_PATH_CACHE_TICKS = 10;
    private static final double MAXIMUM_STRAIGHT_PATH_DEVIATION = 1D;
    private static final double DIRECT_SUPPORT_SCAN_STEP = 0.25D;
    private static final double DIRECT_SUPPORT_PROBE_RADIUS = 0.05D;
    private static final double DIRECT_SUPPORT_VERTICAL_RANGE = RiftCreatureMoveHelper.STANDARD_JUMP_HEIGHT + 0.125D;

    private final RiftCreature creature;
    @Nullable
    private BlockPos cachedWalkingPathOrigin;
    @Nullable
    private BlockPos cachedWalkingPathTarget;
    private long cachedWalkingPathTime = Long.MIN_VALUE;
    private boolean cachedWalkingPathResult;

    public RiftCreaturePathNavigate(RiftCreature creature, World world) {
        super(creature, world);
        this.creature = creature;
    }

    @Override
    protected PathFinder getPathFinder() {
        this.nodeProcessor = new RiftCreatureWalkNodeProcessor((RiftCreature) this.entity, true);
        this.nodeProcessor.setCanEnterDoors(true);
        return new PathFinder(this.nodeProcessor);
    }

    @Override
    protected boolean canNavigate() {
        return this.creature.getNavigationBuilder().getCanWalk() && super.canNavigate();
    }

    @Override
    public boolean tryMoveToEntityLiving(Entity target, double speed) {
        Path path = this.getPathToEntityLiving(target);
        boolean startedPath = path != null && this.setPath(path, speed);
        if (target == this.creature.getAttackTarget()) {
            this.creature.setUnableToPathToTarget(!startedPath);
        }
        return startedPath;
    }

    public boolean hasStraightWalkingPathTo(Entity target) {
        BlockPos origin = new BlockPos(this.creature);
        BlockPos destination = new BlockPos(target);
        long worldTime = this.world.getTotalWorldTime();
        boolean cacheExpired = worldTime - this.cachedWalkingPathTime >= WALKING_PATH_CACHE_TICKS;
        if (cacheExpired || !origin.equals(this.cachedWalkingPathOrigin) || !destination.equals(this.cachedWalkingPathTarget)) {
            this.cachedWalkingPathOrigin = origin;
            this.cachedWalkingPathTarget = destination;
            this.cachedWalkingPathTime = worldTime;
            this.cachedWalkingPathResult = this.findWalkingPath(target, destination);
        }
        return this.cachedWalkingPathResult;
    }

    private boolean findWalkingPath(Entity target, BlockPos destination) {
        if (!this.canNavigate()) return false;

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
        Path path = walkingPathFinder.findPath(chunkCache, this.creature, destination, searchRange);
        PathPoint finalPoint = path == null ? null : path.getFinalPathPoint();
        return finalPoint != null
                && finalPoint.x == destination.getX()
                && finalPoint.y == destination.getY()
                && finalPoint.z == destination.getZ()
                && this.pathIsStraight(path)
                && this.directRouteHasSupport(target);
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
            if (deviationX * deviationX + deviationZ * deviationZ
                    > MAXIMUM_STRAIGHT_PATH_DEVIATION * MAXIMUM_STRAIGHT_PATH_DEVIATION) {
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
