package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
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
import net.minecraftforge.event.ForgeEventFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Ground navigator for creatures for this mod
 * */
public class RiftCreaturePathNavigate extends PathNavigateGround {
    private static final int WALKING_PATH_CACHE_TICKS = 10;
    private static final double MAXIMUM_STRAIGHT_PATH_DEVIATION_SQUARED = 1D;
    private static final double DIRECT_SUPPORT_SCAN_STEP = 0.25D;
    private static final double DIRECT_SUPPORT_PROBE_RADIUS = 0.05D;
    private static final double DIRECT_SUPPORT_VERTICAL_RANGE = RiftCreatureMoveHelper.STANDARD_JUMP_CLEARANCE;
    private static final int BLOCK_BREAK_PATH_CACHE_TICKS = 5;
    private static final double BLOCK_BREAK_SCAN_STEP = 0.25D;
    private static final double BLOCK_BREAK_SHORTCUT_EPSILON = 1E-4D;
    private static final double BLOCK_BREAK_SUPPORT_DEPTH = 0.25D;
    private static final int BLOCK_BREAK_DENIAL_TICKS = 100;

    private RiftCreatureWalkNodeProcessor riftNodeProcessor;
    private final RiftCreature creature;
    @Nullable
    private BlockPos cachedWalkingPathOrigin;
    @Nullable
    private BlockPos cachedWalkingPathTarget;
    private long cachedWalkingPathTime = Long.MIN_VALUE;
    private boolean cachedWalkingPathResult;
    private boolean cachedSafeDownwardWalkingPathResult;
    @Nullable
    private BlockPos cachedBlockBreakOrigin;
    @Nullable
    private BlockPos cachedBlockBreakTarget;
    private long cachedBlockBreakPathTime = Long.MIN_VALUE;
    private boolean cachedBlockBreakPathResult;
    private final Map<BlockPos, BlockBreakPlanEntry> cachedBlockBreakPlan = new HashMap<>();
    @Nullable
    private Path cachedBlockBreakApproachPath;
    @Nullable
    private Vec3d cachedBlockBreakApproachLanding;
    private final Map<BlockPos, Long> temporarilyDeniedBlockBreaks = new HashMap<>();

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
        if (target.isInWater()) return this.tryMoveToEntityLivingUsingWater(target, speed);
        if (!this.canNavigate()) return false;

        Path path = this.getPathToEntityLiving(target);
        boolean startedPath = path != null && this.setPath(path, speed);
        if (target == this.creature.getAttackTarget()) {
            this.creature.setUnableToPathToTarget(!startedPath
                    || path.getCurrentPathLength() <= 1
                    || !this.pathIsSafe(path));
        }
        return startedPath;
    }

    public boolean tryMoveToEntityLivingUsingWater(Entity target, double speed) {
        if (!this.canNavigate()) return false;

        Path path = this.getPathToEntityLiving(target);
        boolean startedPath = path != null && this.pathUsesWater(path) && this.setPath(path, speed);
        if (target == this.creature.getAttackTarget()) {
            this.creature.setUnableToPathToTarget(!startedPath || path.getCurrentPathLength() <= 1);
        }
        return startedPath;
    }

    private boolean pathUsesWater(Path path) {
        for (int index = 0; index < path.getCurrentPathLength(); index++) {
            if (path.getPathPointFromIndex(index).nodeType == PathNodeType.WATER) return true;
        }
        return false;
    }

    private boolean pathReachesTarget(@Nullable Path path, Entity target) {
        if (path == null) return false;

        PathPoint finalPoint = path.getFinalPathPoint();
        if (finalPoint == null) return false;

        int nodeSize = MathHelper.floor(this.creature.width + 1F);
        return finalPoint.x == MathHelper.floor(target.posX - nodeSize * 0.5D)
                && finalPoint.y == MathHelper.floor(target.getEntityBoundingBox().minY)
                && finalPoint.z == MathHelper.floor(target.posZ - nodeSize * 0.5D);
    }

    private boolean pathIsSafe(Path path) {
        for (int index = path.getCurrentPathIndex(); index < path.getCurrentPathLength(); index++) {
            PathNodeType nodeType = path.getPathPointFromIndex(index).nodeType;
            if (this.isDangerous(nodeType)) return false;
        }
        return true;
    }

    private boolean isDangerous(PathNodeType nodeType) {
        return nodeType == PathNodeType.DANGER_FIRE
                || nodeType == PathNodeType.DAMAGE_FIRE
                || nodeType == PathNodeType.DANGER_CACTUS
                || nodeType == PathNodeType.DAMAGE_CACTUS
                || nodeType == PathNodeType.DANGER_OTHER
                || nodeType == PathNodeType.DAMAGE_OTHER;
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

    /**
     * Returns whether breaking the obstructions in a safe direct corridor would provide a
     * shorter route than the creature's normal path, including any available leap path.
     */
    public boolean shouldUseBlockBreakPath(@NotNull Entity target) {
        this.clearExpiredBlockBreakDenials();

        int creatureY = MathHelper.floor(this.creature.getEntityBoundingBox().minY + 1E-3D);
        int targetY = MathHelper.floor(target.getEntityBoundingBox().minY + 1E-3D);
        if (Math.abs(targetY - creatureY) > 1
                || this.creature.bodyTouchingLiquid() && !this.creature.getNavigationBuilder().getCanSwim()) {
            this.invalidateBlockBreakPathCache();
            return false;
        }

        BlockPos origin = new BlockPos(this.creature);
        BlockPos destination = new BlockPos(target);
        long worldTime = this.world.getTotalWorldTime();
        boolean cacheExpired = this.cachedBlockBreakOrigin == null
                || worldTime < this.cachedBlockBreakPathTime
                || worldTime - this.cachedBlockBreakPathTime >= BLOCK_BREAK_PATH_CACHE_TICKS;
        if (cacheExpired || !origin.equals(this.cachedBlockBreakOrigin) || !destination.equals(this.cachedBlockBreakTarget)) {
            this.cachedBlockBreakOrigin = origin;
            this.cachedBlockBreakTarget = destination;
            this.cachedBlockBreakPathTime = worldTime;
            BlockBreakRoute blockBreakRoute = this.calculateBlockBreakPath(target);
            this.cachedBlockBreakPathResult = blockBreakRoute != null;
            this.cachedBlockBreakPlan.clear();
            this.cachedBlockBreakApproachPath = blockBreakRoute == null
                    ? null
                    : blockBreakRoute.approachPath();
            this.cachedBlockBreakApproachLanding = blockBreakRoute == null
                    ? null
                    : blockBreakRoute.approachLanding();
            if (blockBreakRoute != null) {
                for (BlockBreakObstruction obstruction : blockBreakRoute.obstructions().values()) {
                    this.cachedBlockBreakPlan.put(
                            obstruction.blockPos(),
                            new BlockBreakPlanEntry(
                                    obstruction.routeBounds(),
                                    obstruction.routeFeetY(),
                                    obstruction.standardJumpAllowed()
                            )
                    );
                }
            }
        }
        return this.cachedBlockBreakPathResult;
    }

    public void invalidateBlockBreakPathCache() {
        this.cachedBlockBreakOrigin = null;
        this.cachedBlockBreakTarget = null;
        this.cachedBlockBreakPathResult = false;
        this.cachedBlockBreakPlan.clear();
        this.cachedBlockBreakApproachPath = null;
        this.cachedBlockBreakApproachLanding = null;
    }

    @Nullable
    public Vec3d getBlockBreakApproachPosition() {
        if (!this.cachedBlockBreakPathResult || this.cachedBlockBreakApproachLanding == null) {
            return null;
        }
        if (this.cachedBlockBreakApproachPath != null) {
            Path approachPath = this.currentPath != null
                    && this.currentPath.isSamePath(this.cachedBlockBreakApproachPath)
                    ? this.currentPath
                    : this.cachedBlockBreakApproachPath;
            if (!approachPath.isFinished()) return approachPath.getPosition(this.creature);
        }
        return this.cachedBlockBreakApproachLanding;
    }

    public boolean tryMoveAlongBlockBreakApproach(double speed) {
        if (!this.cachedBlockBreakPathResult
                || this.cachedBlockBreakApproachLanding == null) {
            return false;
        }
        if (this.cachedBlockBreakApproachPath != null) {
            if (this.currentPath != null && this.currentPath.isSamePath(this.cachedBlockBreakApproachPath)) {
                if (!this.currentPath.isFinished()) {
                    this.setSpeed(speed);
                    return true;
                }
            }
            else if (!this.cachedBlockBreakApproachPath.isFinished()
                    && this.setPath(this.cachedBlockBreakApproachPath, speed)) {
                return true;
            }
        }

        double x = this.cachedBlockBreakApproachLanding.x - this.creature.posX;
        double y = this.cachedBlockBreakApproachLanding.y - this.creature.getEntityBoundingBox().minY;
        double z = this.cachedBlockBreakApproachLanding.z - this.creature.posZ;
        if (x * x + z * z <= BLOCK_BREAK_SCAN_STEP * BLOCK_BREAK_SCAN_STEP
                && Math.abs(y) <= BLOCK_BREAK_SCAN_STEP) {
            this.cachedBlockBreakApproachPath = null;
            this.cachedBlockBreakApproachLanding = null;
            return false;
        }

        this.clearPath();
        this.creature.getMoveHelper().setMoveTo(
                this.cachedBlockBreakApproachLanding.x,
                this.cachedBlockBreakApproachLanding.y,
                this.cachedBlockBreakApproachLanding.z,
                speed
        );
        return true;
    }

    @NotNull
    public Map<BlockPos, BlockBreakPlanEntry> copyPlannedBlockBreaks() {
        return new HashMap<>(this.cachedBlockBreakPlan);
    }

    @Nullable
    public BlockBreakPlanEntry getPlannedBlockBreak(@NotNull BlockPos blockPos) {
        return this.cachedBlockBreakPathResult ? this.cachedBlockBreakPlan.get(blockPos) : null;
    }

    public void markBlockBreakDenied(@NotNull BlockPos blockPos) {
        this.temporarilyDeniedBlockBreaks.put(
                blockPos.toImmutable(),
                this.world.getTotalWorldTime() + BLOCK_BREAK_DENIAL_TICKS
        );
        this.invalidateBlockBreakPathCache();
    }

    public boolean isBlockBreakTemporarilyDenied(@NotNull BlockPos blockPos) {
        Long denialEndTime = this.temporarilyDeniedBlockBreaks.get(blockPos);
        if (denialEndTime == null) return false;
        if (this.world.getTotalWorldTime() < denialEndTime) return true;

        this.temporarilyDeniedBlockBreaks.remove(blockPos);
        return false;
    }

    private void clearExpiredBlockBreakDenials() {
        long worldTime = this.world.getTotalWorldTime();
        this.temporarilyDeniedBlockBreaks.entrySet().removeIf(entry -> entry.getValue() <= worldTime);
    }

    @Nullable
    private BlockBreakRoute calculateBlockBreakPath(Entity target) {
        if (!ForgeEventFactory.getMobGriefingEvent(this.world, this.creature)) return null;

        BlockBreakRoute directRoute = this.findBlockBreakRoute(target);
        if (directRoute == null) return null;

        Path ordinaryPath = this.findFreshPathToEntityLiving(target);
        boolean ordinaryPathIsUsable = ordinaryPath != null
                && this.pathIsSafe(ordinaryPath)
                && this.pathLeapArcsAreClear(ordinaryPath);
        BlockBreakRoute plannedRoute = ordinaryPathIsUsable
                ? this.removeLeapTraversedObstructions(ordinaryPath, directRoute, target)
                : directRoute;
        if (plannedRoute == null || !this.routeObstructionsAreBreakable(plannedRoute)) return null;
        if (!ordinaryPathIsUsable || !this.pathReachesTarget(ordinaryPath, target)) return plannedRoute;
        return this.pathLength(ordinaryPath, target)
                > plannedRoute.length() + BLOCK_BREAK_SHORTCUT_EPSILON
                ? plannedRoute
                : null;
    }

    @Nullable
    private Path findFreshPathToEntityLiving(@NotNull Entity target) {
        Path activePath = this.currentPath;
        this.currentPath = null;
        Path freshPath = this.getPathToEntityLiving(target);
        this.currentPath = activePath;
        return freshPath;
    }

    private boolean routeObstructionsAreBreakable(@NotNull BlockBreakRoute route) {
        for (BlockBreakObstruction obstruction : route.obstructions().values()) {
            if (!obstruction.breakable()) return false;
        }
        return true;
    }

    private boolean pathLeapArcsAreClear(@NotNull Path path) {
        int firstEdgeEndIndex = Math.max(1, path.getCurrentPathIndex());
        for (int index = firstEdgeEndIndex; index < path.getCurrentPathLength(); index++) {
            PathPoint from = path.getPathPointFromIndex(index - 1);
            PathPoint to = path.getPathPointFromIndex(index);
            if (this.riftNodeProcessor.isLeapEdge(from, to)
                    && !this.riftNodeProcessor.hasClearLeapArc(from, to)) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    private BlockBreakRoute removeLeapTraversedObstructions(
            @NotNull Path path,
            @NotNull BlockBreakRoute route,
            @NotNull Entity target
    ) {
        double leapClearance = this.creature.getNavigationBuilder().getLeapHeight() + 1E-3D;
        Set<BlockPos> leapTraversedObstructions = new HashSet<>();
        Map<Integer, Double> firstObstructionDistanceByLeapEdge = new HashMap<>();
        int firstEdgeEndIndex = Math.max(1, path.getCurrentPathIndex());
        for (int index = firstEdgeEndIndex; index < path.getCurrentPathLength(); index++) {
            PathPoint from = path.getPathPointFromIndex(index - 1);
            PathPoint to = path.getPathPointFromIndex(index);
            if (!this.riftNodeProcessor.isLeapEdge(from, to)
                    || !this.riftNodeProcessor.hasClearLeapArc(from, to)) {
                continue;
            }

            for (BlockBreakObstruction obstruction : route.obstructions().values()) {
                if (obstruction.collisionBounds().maxY - obstruction.routeFeetY() <= leapClearance
                        && this.pathEdgeCrossesObstruction(from, to, obstruction)) {
                    leapTraversedObstructions.add(obstruction.blockPos());
                    firstObstructionDistanceByLeapEdge.merge(
                            index,
                            obstruction.firstDistance(),
                            Math::min
                    );
                }
            }
        }
        if (leapTraversedObstructions.isEmpty()) return route;

        Map<BlockPos, BlockBreakObstruction> remainingObstructions = new HashMap<>(route.obstructions());
        leapTraversedObstructions.forEach(remainingObstructions::remove);
        if (remainingObstructions.isEmpty()) return null;

        double firstRemainingObstructionDistance = Double.POSITIVE_INFINITY;
        for (BlockBreakObstruction obstruction : remainingObstructions.values()) {
            firstRemainingObstructionDistance = Math.min(
                    firstRemainingObstructionDistance,
                    obstruction.firstDistance()
            );
        }

        Path approachPath = null;
        Vec3d approachLanding = null;
        double plannedLength = route.length();
        for (int index = firstEdgeEndIndex; index < path.getCurrentPathLength(); index++) {
            Double firstTraversedDistance = firstObstructionDistanceByLeapEdge.get(index);
            if (firstTraversedDistance == null
                    || firstTraversedDistance + BLOCK_BREAK_SHORTCUT_EPSILON
                    >= firstRemainingObstructionDistance) {
                continue;
            }

            approachPath = this.copyPathThrough(path, index);
            PathPoint landing = path.getPathPointFromIndex(index);
            double centerOffset = MathHelper.floor(this.creature.width + 1F) * 0.5D;
            approachLanding = new Vec3d(
                    landing.x + centerOffset,
                    landing.y,
                    landing.z + centerOffset
            );
            plannedLength = this.pathLengthThrough(path, index)
                    + this.distanceFromPathPointToTarget(landing, target);
            break;
        }
        return new BlockBreakRoute(
                plannedLength,
                remainingObstructions,
                approachPath,
                approachLanding
        );
    }

    @NotNull
    private Path copyPathThrough(@NotNull Path path, int finalIndex) {
        int firstIndex = Math.min(path.getCurrentPathIndex(), finalIndex);
        PathPoint[] points = new PathPoint[finalIndex - firstIndex + 1];
        for (int index = firstIndex; index <= finalIndex; index++) {
            points[index - firstIndex] = path.getPathPointFromIndex(index);
        }
        return new Path(points);
    }

    private boolean pathEdgeCrossesObstruction(
            @NotNull PathPoint from,
            @NotNull PathPoint to,
            @NotNull BlockBreakObstruction obstruction
    ) {
        double centerOffset = MathHelper.floor(this.creature.width + 1F) * 0.5D;
        double fromX = from.x + centerOffset;
        double fromZ = from.z + centerOffset;
        double toX = to.x + centerOffset;
        double toZ = to.z + centerOffset;
        double displacementX = toX - fromX;
        double displacementZ = toZ - fromZ;
        double horizontalDistance = Math.hypot(displacementX, displacementZ);
        int samples = Math.max(1, (int)Math.ceil(horizontalDistance / BLOCK_BREAK_SCAN_STEP));
        AxisAlignedBB creatureBounds = this.creature.getEntityBoundingBox();
        double halfWidthX = (creatureBounds.maxX - creatureBounds.minX) * 0.5D;
        double halfWidthZ = (creatureBounds.maxZ - creatureBounds.minZ) * 0.5D;
        AxisAlignedBB collisionBounds = obstruction.collisionBounds();

        for (int sample = 0; sample <= samples; sample++) {
            double progress = (double)sample / samples;
            double centerX = fromX + displacementX * progress;
            double centerZ = fromZ + displacementZ * progress;
            if (collisionBounds.maxX > centerX - halfWidthX + 1E-5D
                    && collisionBounds.minX < centerX + halfWidthX - 1E-5D
                    && collisionBounds.maxZ > centerZ - halfWidthZ + 1E-5D
                    && collisionBounds.minZ < centerZ + halfWidthZ - 1E-5D) {
                return true;
            }
        }
        return false;
    }

    private double pathLength(@NotNull Path path, @NotNull Entity target) {
        int finalIndex = path.getCurrentPathLength() - 1;
        if (finalIndex < path.getCurrentPathIndex()) return this.creature.getDistance(target);
        return this.pathLengthThrough(path, finalIndex)
                + this.distanceFromPathPointToTarget(path.getPathPointFromIndex(finalIndex), target);
    }

    private double pathLengthThrough(@NotNull Path path, int finalIndex) {
        double previousX = this.creature.posX;
        double previousY = this.creature.getEntityBoundingBox().minY;
        double previousZ = this.creature.posZ;
        double length = 0D;
        double centerOffset = MathHelper.floor(this.creature.width + 1F) * 0.5D;

        for (int index = path.getCurrentPathIndex(); index <= finalIndex; index++) {
            PathPoint point = path.getPathPointFromIndex(index);
            double pointX = point.x + centerOffset;
            double pointY = point.y;
            double pointZ = point.z + centerOffset;
            double x = pointX - previousX;
            double y = pointY - previousY;
            double z = pointZ - previousZ;
            length += Math.sqrt(x * x + y * y + z * z);
            previousX = pointX;
            previousY = pointY;
            previousZ = pointZ;
        }
        return length;
    }

    private double distanceFromPathPointToTarget(@NotNull PathPoint point, @NotNull Entity target) {
        double centerOffset = MathHelper.floor(this.creature.width + 1F) * 0.5D;
        double x = target.posX - (point.x + centerOffset);
        double y = target.getEntityBoundingBox().minY - point.y;
        double z = target.posZ - (point.z + centerOffset);
        return Math.sqrt(x * x + y * y + z * z);
    }

    @Nullable
    private BlockBreakRoute findBlockBreakRoute(Entity target) {
        double displacementX = target.posX - this.creature.posX;
        double displacementZ = target.posZ - this.creature.posZ;
        double horizontalDistance = Math.sqrt(displacementX * displacementX + displacementZ * displacementZ);
        if (horizontalDistance < 1E-4D) return null;

        double directionX = displacementX / horizontalDistance;
        double directionZ = displacementZ / horizontalDistance;
        AxisAlignedBB startingBounds = this.creature.getEntityBoundingBox();
        AxisAlignedBB targetBounds = target.getEntityBoundingBox();
        double creatureRadius = this.projectedHorizontalRadius(startingBounds, directionX, directionZ);
        double targetRadius = this.projectedHorizontalRadius(targetBounds, directionX, directionZ);
        double travelDistance = horizontalDistance - creatureRadius - targetRadius;
        if (travelDistance <= BLOCK_BREAK_SHORTCUT_EPSILON) return null;

        int samples = Math.max(1, (int)Math.ceil(travelDistance / BLOCK_BREAK_SCAN_STEP));
        RiftCreatureWalkNodeProcessor directNodeProcessor = new RiftCreatureWalkNodeProcessor(this.creature, false);
        directNodeProcessor.init(this.world, this.creature);
        Map<BlockPos, BlockBreakObstruction> routeObstructions = new HashMap<>();
        double routeFeetY = startingBounds.minY;
        double targetFeetY = targetBounds.minY;
        double previousX = this.creature.posX;
        double previousZ = this.creature.posZ;
        double routeLength = 0D;

        boolean routeInvalid = false;
        for (int sample = 1; sample <= samples; sample++) {
            double sampleDistance = travelDistance * sample / samples;
            double offsetX = directionX * sampleDistance;
            double offsetZ = directionZ * sampleDistance;
            BlockBreakRouteSample routeSample = this.findBlockBreakRouteSample(
                    startingBounds,
                    offsetX,
                    offsetZ,
                    sampleDistance,
                    routeFeetY,
                    targetFeetY,
                    directNodeProcessor
            );
            if (routeSample == null) {
                routeInvalid = true;
                break;
            }

            double sampleX = (routeSample.bounds().minX + routeSample.bounds().maxX) * 0.5D;
            double sampleZ = (routeSample.bounds().minZ + routeSample.bounds().maxZ) * 0.5D;
            routeLength += Math.sqrt(
                    Math.pow(sampleX - previousX, 2D)
                            + Math.pow(routeSample.bounds().minY - routeFeetY, 2D)
                            + Math.pow(sampleZ - previousZ, 2D)
            );
            routeFeetY = routeSample.bounds().minY;
            previousX = sampleX;
            previousZ = sampleZ;
            for (BlockBreakObstruction obstruction : routeSample.obstructions().values()) {
                routeObstructions.merge(
                        obstruction.blockPos(),
                        obstruction,
                        this::mergeBlockBreakObstructions
                );
            }
        }
        BlockBreakRoute route = null;
        if (!routeInvalid && !routeObstructions.isEmpty()) {
            routeLength += Math.sqrt(
                    Math.pow(target.posX - previousX, 2D)
                            + Math.pow(targetFeetY - routeFeetY, 2D)
                            + Math.pow(target.posZ - previousZ, 2D)
            );
            route = new BlockBreakRoute(routeLength, routeObstructions, null, null);
        }
        directNodeProcessor.postProcess();
        return route;
    }

    @Nullable
    private BlockBreakRouteSample findBlockBreakRouteSample(
            AxisAlignedBB startingBounds,
            double offsetX,
            double offsetZ,
            double sampleDistance,
            double previousFeetY,
            double targetFeetY,
            RiftCreatureWalkNodeProcessor directNodeProcessor
    ) {
        double[] candidateFeetLevels = new double[]{
                previousFeetY,
                startingBounds.minY,
                targetFeetY
        };
        BlockBreakRouteSample bestSample = null;
        for (int index = 0; index < candidateFeetLevels.length; index++) {
            double candidateFeetY = candidateFeetLevels[index];
            boolean duplicate = false;
            for (int priorIndex = 0; priorIndex < index; priorIndex++) {
                if (Math.abs(candidateFeetY - candidateFeetLevels[priorIndex]) < BLOCK_BREAK_SHORTCUT_EPSILON) {
                    duplicate = true;
                    break;
                }
            }
            if (duplicate) continue;

            AxisAlignedBB candidateBounds = startingBounds.offset(
                    offsetX,
                    candidateFeetY - startingBounds.minY,
                    offsetZ
            );
            BlockBreakRouteSample candidate = this.evaluateBlockBreakRouteSample(
                    candidateBounds,
                    sampleDistance,
                    previousFeetY,
                    startingBounds.minY,
                    targetFeetY,
                    directNodeProcessor
            );
            if (candidate == null) continue;

            int candidateUnbreakableCount = this.unbreakableObstructionCount(candidate);
            int bestUnbreakableCount = bestSample == null
                    ? Integer.MAX_VALUE
                    : this.unbreakableObstructionCount(bestSample);
            if (bestSample == null
                    || candidateUnbreakableCount < bestUnbreakableCount
                    || candidateUnbreakableCount == bestUnbreakableCount
                    && candidate.obstructions().size() < bestSample.obstructions().size()
                    || candidateUnbreakableCount == bestUnbreakableCount
                    && candidate.obstructions().size() == bestSample.obstructions().size()
                    && Math.abs(candidate.bounds().minY - previousFeetY)
                    < Math.abs(bestSample.bounds().minY - previousFeetY)) {
                bestSample = candidate;
            }
        }
        return bestSample;
    }

    private int unbreakableObstructionCount(@NotNull BlockBreakRouteSample sample) {
        int count = 0;
        for (BlockBreakObstruction obstruction : sample.obstructions().values()) {
            if (!obstruction.breakable()) count++;
        }
        return count;
    }

    @Nullable
    private BlockBreakRouteSample evaluateBlockBreakRouteSample(
            AxisAlignedBB bounds,
            double sampleDistance,
            double previousFeetY,
            double startingFeetY,
            double targetFeetY,
            RiftCreatureWalkNodeProcessor directNodeProcessor
    ) {
        boolean swimmableRoute = this.routeCrossesSwimmableBlock(bounds);
        if (!this.hasFullRouteSupport(bounds, startingFeetY, targetFeetY) && !swimmableRoute) {
            return null;
        }

        int floorY = MathHelper.floor(bounds.minY + 1E-3D);
        BlockPos minimum = new BlockPos(
                Math.floor(bounds.minX + 1E-5D),
                floorY,
                Math.floor(bounds.minZ + 1E-5D)
        );
        BlockPos maximum = new BlockPos(
                Math.ceil(bounds.maxX - 1E-5D) - 1D,
                Math.ceil(bounds.maxY - 1E-5D) - 1D,
                Math.ceil(bounds.maxZ - 1E-5D) - 1D
        );

        for (int x = minimum.getX(); x <= maximum.getX(); x++) {
            for (int z = minimum.getZ(); z <= maximum.getZ(); z++) {
                PathNodeType nodeType = directNodeProcessor.getPathNodeType(
                        this.world, x, floorY, z,
                        this.creature, 1, 1, 1, false, false
                );
                if (this.isDangerous(nodeType)) return null;
            }
        }

        boolean standardJumpAllowed = this.creature.getNavigationBuilder().getCanWalk() && !swimmableRoute;
        Map<BlockPos, BlockBreakObstruction> obstructions = new HashMap<>();
        for (BlockPos blockPos : BlockPos.getAllInBoxMutable(minimum, maximum)) {
            BlockPos immutablePos = blockPos.toImmutable();
            IBlockState blockState = this.world.getBlockState(immutablePos);
            AxisAlignedBB collisionBounds = blockState.getCollisionBoundingBox(this.world, immutablePos);
            if (collisionBounds == null) continue;

            AxisAlignedBB worldCollisionBounds = collisionBounds.offset(immutablePos);
            if (!worldCollisionBounds.intersects(bounds)) continue;

            BlockBreakPlanEntry planEntry = new BlockBreakPlanEntry(bounds, previousFeetY, standardJumpAllowed);
            if (standardJumpAllowed && this.isStandardJumpable(planEntry, worldCollisionBounds)
                    || !standardJumpAllowed
                    && worldCollisionBounds.maxY <= previousFeetY + this.creature.stepHeight + 1E-3D
            ) {
                continue;
            }
            obstructions.put(immutablePos, new BlockBreakObstruction(
                    immutablePos,
                    worldCollisionBounds,
                    bounds,
                    previousFeetY,
                    sampleDistance,
                    standardJumpAllowed,
                    !this.isBlockBreakTemporarilyDenied(blockPos) && this.creature.canBreakBlock(blockPos)
            ));
        }
        return new BlockBreakRouteSample(bounds, obstructions);
    }

    private BlockBreakObstruction mergeBlockBreakObstructions(@NotNull BlockBreakObstruction first, @NotNull BlockBreakObstruction second) {
        BlockBreakObstruction lowerApproach = first.routeFeetY() <= second.routeFeetY() ? first : second;
        return new BlockBreakObstruction(
                first.blockPos(),
                first.collisionBounds(),
                lowerApproach.routeBounds(),
                Math.min(first.routeFeetY(), second.routeFeetY()),
                Math.min(first.firstDistance(), second.firstDistance()),
                first.standardJumpAllowed() && second.standardJumpAllowed(),
                first.breakable() && second.breakable()
        );
    }

    public boolean isStandardJumpable(@NotNull BlockBreakPlanEntry planEntry, @NotNull AxisAlignedBB collisionBounds) {
        if (!planEntry.standardJumpAllowed() || !this.creature.getNavigationBuilder().getCanWalk()
                || this.creature.bodyTouchingLiquid()
                || collisionBounds.maxY > planEntry.routeFeetY() + DIRECT_SUPPORT_VERTICAL_RANGE + 1E-3D
        ) {
            return false;
        }

        AxisAlignedBB raisedBounds = planEntry.routeBounds().offset(
                0D,
                collisionBounds.maxY - planEntry.routeBounds().minY,
                0D
        );
        return !this.world.collidesWithAnyBlock(raisedBounds);
    }

    private boolean hasFullRouteSupport(AxisAlignedBB bounds, double startingFeetY, double targetFeetY) {
        double insetX = Math.min(DIRECT_SUPPORT_PROBE_RADIUS, (bounds.maxX - bounds.minX) * 0.25D);
        double insetZ = Math.min(DIRECT_SUPPORT_PROBE_RADIUS, (bounds.maxZ - bounds.minZ) * 0.25D);
        double minimumX = bounds.minX + insetX;
        double maximumX = bounds.maxX - insetX;
        double minimumZ = bounds.minZ + insetZ;
        double maximumZ = bounds.maxZ - insetZ;
        double centerX = (bounds.minX + bounds.maxX) * 0.5D;
        double centerZ = (bounds.minZ + bounds.maxZ) * 0.5D;

        //The center selects the discrete floor level. Corners may straddle the lower and
        //upper surfaces during a normal one-block step, but every point still needs support.
        return this.hasRouteSupportAt(centerX, centerZ, bounds.minY)
                && this.hasRouteSupportAtEitherLevel(minimumX, minimumZ, startingFeetY, targetFeetY)
                && this.hasRouteSupportAtEitherLevel(minimumX, maximumZ, startingFeetY, targetFeetY)
                && this.hasRouteSupportAtEitherLevel(maximumX, minimumZ, startingFeetY, targetFeetY)
                && this.hasRouteSupportAtEitherLevel(maximumX, maximumZ, startingFeetY, targetFeetY);
    }

    private boolean hasRouteSupportAtEitherLevel(double x, double z, double startingFeetY, double targetFeetY) {
        return this.hasRouteSupportAt(x, z, startingFeetY)
                || Math.abs(targetFeetY - startingFeetY) >= BLOCK_BREAK_SHORTCUT_EPSILON
                && this.hasRouteSupportAt(x, z, targetFeetY);
    }

    private boolean hasRouteSupportAt(double x, double z, double feetY) {
        AxisAlignedBB supportProbe = new AxisAlignedBB(
                x - 1E-3D,
                feetY - BLOCK_BREAK_SUPPORT_DEPTH,
                z - 1E-3D,
                x + 1E-3D,
                feetY + 1E-3D,
                z + 1E-3D
        );
        BlockPos minimum = new BlockPos(supportProbe.minX, supportProbe.minY, supportProbe.minZ);
        BlockPos maximum = new BlockPos(supportProbe.maxX, supportProbe.maxY, supportProbe.maxZ);
        for (BlockPos blockPos : BlockPos.getAllInBoxMutable(minimum, maximum)) {
            IBlockState blockState = this.world.getBlockState(blockPos);
            AxisAlignedBB collisionBounds = blockState.getCollisionBoundingBox(this.world, blockPos);
            if (collisionBounds == null) continue;

            AxisAlignedBB worldCollisionBounds = collisionBounds.offset(blockPos);
            if (worldCollisionBounds.maxY <= feetY + 1E-3D
                    && worldCollisionBounds.maxY >= feetY - BLOCK_BREAK_SUPPORT_DEPTH - 1E-3D
                    && worldCollisionBounds.intersects(supportProbe)) {
                return true;
            }
        }
        return false;
    }

    private double projectedHorizontalRadius(AxisAlignedBB bounds, double directionX, double directionZ) {
        double halfWidthX = (bounds.maxX - bounds.minX) * 0.5D;
        double halfWidthZ = (bounds.maxZ - bounds.minZ) * 0.5D;
        return Math.abs(directionX) * halfWidthX + Math.abs(directionZ) * halfWidthZ;
    }

    private record BlockBreakRoute(
            double length,
            Map<BlockPos, BlockBreakObstruction> obstructions,
            @Nullable Path approachPath,
            @Nullable Vec3d approachLanding
    ) {}

    private record BlockBreakRouteSample(AxisAlignedBB bounds, Map<BlockPos, BlockBreakObstruction> obstructions) {}

    private record BlockBreakObstruction(
            BlockPos blockPos,
            AxisAlignedBB collisionBounds,
            AxisAlignedBB routeBounds,
            double routeFeetY,
            double firstDistance,
            boolean standardJumpAllowed,
            boolean breakable
    ) {}

    public record BlockBreakPlanEntry(AxisAlignedBB routeBounds, double routeFeetY, boolean standardJumpAllowed) {}

    private boolean routeCrossesSwimmableBlock(AxisAlignedBB bounds) {
        if (!this.creature.getNavigationBuilder().getCanSwim()) return false;

        BlockPos minimum = new BlockPos(bounds.minX, bounds.minY, bounds.minZ);
        BlockPos maximum = new BlockPos(bounds.maxX, bounds.minY, bounds.maxZ);
        for (BlockPos blockPos : BlockPos.getAllInBoxMutable(minimum, maximum)) {
            IBlockState blockState = this.world.getBlockState(blockPos);
            if (blockState.getMaterial() != Material.WATER) continue;

            String registryName = String.valueOf(Block.REGISTRY.getNameForObject(blockState.getBlock()));
            for (String swimmingBlock : this.creature.getNavigationBuilder().getSwimmingBlockWhitelist()) {
                if (swimmingBlock.equals(registryName)) return true;
            }
        }
        return false;
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
