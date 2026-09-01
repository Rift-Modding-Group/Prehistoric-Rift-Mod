package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveResult.MoveResult;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class RiftCreatureWalkNodeProcessor extends WalkNodeProcessor {
    private static final int WATER_SURFACE_SEARCH_DEPTH = 4;
    private static final float WATER_PATH_COST = 2f;

    @NotNull
    private final RiftCreature creature;
    private final boolean allowLeaps;
    private boolean waterPathingAllowed;

    public RiftCreatureWalkNodeProcessor(@NotNull RiftCreature creature, boolean allowLeaps) {
        this.creature = creature;
        this.allowLeaps = allowLeaps;
    }

    @Override
    public void init(IBlockAccess source, EntityLiving mob) {
        super.init(source, mob);
        if (!this.waterPathingAllowed) mob.setPathPriority(PathNodeType.WATER, -1f);
    }

    @Override
    protected PathPoint openPoint(int x, int y, int z) {
        int hash = PathPoint.makeHash(x, y, z);
        PathPoint pathPoint = this.pointMap.lookup(hash);
        if (pathPoint == null) {
            pathPoint = new LeapAwarePathPoint(x, y, z);
            this.pointMap.addKey(hash, pathPoint);
        }
        return pathPoint;
    }

    @Override
    public PathPoint getStart() {
        int x = MathHelper.floor(this.creature.posX - this.entitySizeX * 0.5D);
        int z = MathHelper.floor(this.creature.posZ - this.entitySizeZ * 0.5D);

        if (this.waterPathingAllowed && this.creature.bodyTouchingLiquid()) {
            int surfaceY = this.findWaterSurfaceY(
                    MathHelper.floor(this.creature.posX),
                    MathHelper.floor(this.creature.posY),
                    MathHelper.floor(this.creature.posZ)
            );
            if (surfaceY != Integer.MIN_VALUE) {
                PathPoint start = this.openPoint(x, surfaceY, z);
                start.nodeType = PathNodeType.WATER;
                start.costMalus = WATER_PATH_COST;
                return start;
            }
        }

        PathPoint vanillaStart = super.getStart();
        PathNodeType startType = this.getNodeType(x, vanillaStart.y, z);
        float priority = this.creature.getPathPriority(startType);
        if (priority < 0F) return vanillaStart;

        PathPoint centeredStart = this.openPoint(x, vanillaStart.y, z);
        centeredStart.nodeType = startType;
        centeredStart.costMalus = Math.max(centeredStart.costMalus, priority);
        return centeredStart;
    }

    @Override
    public PathPoint getPathPointToCoords(double x, double y, double z) {
        int targetY = MathHelper.floor(y);
        int targetX = MathHelper.floor(x - this.entitySizeX * 0.5D);
        int targetZ = MathHelper.floor(z - this.entitySizeZ * 0.5D);
        int surfaceY = this.waterPathingAllowed
                ? this.findWaterSurfaceY(MathHelper.floor(x), targetY, MathHelper.floor(z))
                : Integer.MIN_VALUE;
        if (surfaceY == Integer.MIN_VALUE) return this.openPoint(targetX, targetY, targetZ);

        PathPoint target = this.openPoint(targetX, surfaceY, targetZ);
        target.nodeType = PathNodeType.WATER;
        target.costMalus = WATER_PATH_COST;
        return target;
    }

    @Override
    public PathNodeType getPathNodeType(IBlockAccess blockAccess, int x, int y, int z) {
        PathNodeType nodeType = super.getPathNodeType(blockAccess, x, y, z);
        return this.waterPathingAllowed
                && nodeType == PathNodeType.OPEN
                && this.isWaterSurface(blockAccess, x, y, z)
                ? PathNodeType.WATER
                : nodeType;
    }

    @Override
    public int findPathOptions(PathPoint[] pathOptions, PathPoint currentPoint, PathPoint targetPoint, float maxDistance) {
        int count = super.findPathOptions(pathOptions, currentPoint, targetPoint, maxDistance);
        if (this.waterPathingAllowed) {
            for (int index = 0; index < count; index++) {
                if (pathOptions[index].nodeType == PathNodeType.WATER) pathOptions[index].costMalus = WATER_PATH_COST;
            }
            count = this.addWaterSurfaceOptions(pathOptions, count, currentPoint, targetPoint, maxDistance);
        }
        if (!this.allowLeaps || !this.creature.getNavigationBuilder().getCanLeap()
                || !this.creature.canUseStamina(MoveResult.LEAP.staminaConsumption())
                || this.creature.bodyTouchingLiquid()
                || currentPoint.nodeType == PathNodeType.WATER
                || this.isWaterSurfaceNode(currentPoint.x, currentPoint.y, currentPoint.z)
        ) {
            return count;
        }

        int walkingOptionCount = count;

        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
            //a leap is a fallback edge. prefer every valid walking edge, including a survivable
            //drop, so creatures walk off safe ledges instead of leaping over them.
            if (this.containsWalkingOption(pathOptions, walkingOptionCount, currentPoint, facing)) continue;

            count = this.addLeapOption(pathOptions, count, currentPoint,
                    this.findUpwardLeapLanding(currentPoint, facing, maxDistance), targetPoint, maxDistance);
            count = this.addLeapOption(pathOptions, count, currentPoint,
                    this.findLevelLeapLanding(currentPoint, facing, maxDistance), targetPoint, maxDistance);
        }
        return count;
    }

    private boolean containsWalkingOption(PathPoint[] points, int count, PathPoint currentPoint, EnumFacing facing) {
        int adjacentX = currentPoint.x + facing.getXOffset();
        int adjacentZ = currentPoint.z + facing.getZOffset();
        for (int index = 0; index < count; index++) {
            PathPoint point = points[index];
            if (point.x == adjacentX && point.z == adjacentZ && point.nodeType != PathNodeType.WATER) return true;
        }
        return false;
    }

    private int addWaterSurfaceOptions(
            PathPoint[] pathOptions,
            int count,
            PathPoint currentPoint,
            PathPoint targetPoint,
            float maxDistance
    ) {
        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
            int x = currentPoint.x + facing.getXOffset();
            int z = currentPoint.z + facing.getZOffset();
            int surfaceY = this.findWaterSurfaceY(x + this.entitySizeX / 2, currentPoint.y, z + this.entitySizeZ / 2);
            if (surfaceY == Integer.MIN_VALUE || Math.abs(surfaceY - currentPoint.y) > 1) continue;

            PathNodeType nodeType = this.getNodeType(x, surfaceY, z);
            float priority = this.creature.getPathPriority(nodeType);
            if (nodeType != PathNodeType.WATER || priority < 0F) continue;

            PathPoint waterPoint = this.openPoint(x, surfaceY, z);
            waterPoint.nodeType = PathNodeType.WATER;
            waterPoint.costMalus = WATER_PATH_COST;
            if (!waterPoint.visited
                    && waterPoint.distanceTo(targetPoint) < maxDistance
                    && !this.contains(pathOptions, count, waterPoint)) {
                pathOptions[count++] = waterPoint;
            }
        }
        return count;
    }

    private int addLeapOption(
            PathPoint[] pathOptions,
            int count,
            PathPoint currentPoint,
            @Nullable PathPoint leapPoint,
            PathPoint targetPoint,
            float maxDistance
    ) {
        if (leapPoint != null && !leapPoint.visited && leapPoint.distanceTo(targetPoint) < maxDistance && !this.contains(pathOptions, count, leapPoint)) {
            pathOptions[count++] = leapPoint;
            if (leapPoint instanceof LeapAwarePathPoint leapAwarePathPoint) {
                leapAwarePathPoint.addLeapPredecessor(currentPoint);
            }
        }
        return count;
    }

    boolean isLeapEdge(@NotNull PathPoint from, @NotNull PathPoint to) {
        return to instanceof LeapAwarePathPoint leapAwarePathPoint
                && leapAwarePathPoint.hasLeapPredecessor(from);
    }

    @Nullable
    private PathPoint findUpwardLeapLanding(PathPoint currentPoint, EnumFacing facing, float maxPathDistance) {
        double ledgeHeight = this.getLeadingObstacleHeight(currentPoint, facing);
        if (ledgeHeight <= RiftCreatureMoveHelper.STANDARD_JUMP_CLEARANCE + 1E-3D || ledgeHeight > this.creature.getNavigationBuilder().getLeapHeight() + 1E-3D) {
            return null;
        }

        int landingY = currentPoint.y + (int)Math.ceil(ledgeHeight - 1E-3D);
        if (landingY - currentPoint.y > this.creature.getNavigationBuilder().getLeapHeight() + 1E-3D) {
            return null;
        }
        int maximumDistance = this.getMaximumLeapDistance(maxPathDistance);
        for (int distance = 1; distance <= maximumDistance; distance++) {
            int landingX = currentPoint.x + facing.getXOffset() * distance;
            int landingZ = currentPoint.z + facing.getZOffset() * distance;
            PathPoint landing = this.getWalkableLanding(
                    landingX, landingY, landingZ, (float)ledgeHeight + distance, true
            );
            if (landing != null) return landing;
        }
        return null;
    }

    @Nullable
    private PathPoint getWalkableLanding(int x, int y, int z, float extraCost, boolean requireFullSupport) {
        PathNodeType nodeType = this.getNodeType(x, y, z);
        float priority = this.creature.getPathPriority(nodeType);
        if (nodeType != PathNodeType.WALKABLE || priority < 0F
                || requireFullSupport && !this.hasFullLandingSupport(x, y, z)) {
            return null;
        }

        PathPoint landing = this.openPoint(x, y, z);
        landing.nodeType = nodeType;
        landing.costMalus = Math.max(landing.costMalus, priority + extraCost);
        return landing;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean hasFullLandingSupport(int landingX, int landingY, int landingZ) {
        for (int x = landingX; x < landingX + this.entitySizeX; x++) {
            for (int z = landingZ; z < landingZ + this.entitySizeZ; z++) {
                if (this.getPathNodeType(this.blockaccess, x, landingY, z) != PathNodeType.WALKABLE) return false;
            }
        }
        return true;
    }

    private double getLeadingObstacleHeight(PathPoint currentPoint, EnumFacing facing) {
        return this.getLeadingObstacleHeight(currentPoint, facing, this.blockaccess);
    }

    private double getLeadingObstacleHeight(
            PathPoint currentPoint,
            EnumFacing facing,
            IBlockAccess blockAccess
    ) {
        int offsetX = facing.getXOffset();
        int offsetZ = facing.getZOffset();
        int minimumX = currentPoint.x + (offsetX > 0 ? this.entitySizeX : offsetX);
        int maximumX = minimumX + (offsetX == 0 ? this.entitySizeX - 1 : 0);
        int minimumZ = currentPoint.z + (offsetZ > 0 ? this.entitySizeZ : offsetZ);
        int maximumZ = minimumZ + (offsetZ == 0 ? this.entitySizeZ - 1 : 0);

        double obstacleHeight = 0D;
        for (int x = minimumX; x <= maximumX; x++) {
            for (int z = minimumZ; z <= maximumZ; z++) {
                obstacleHeight = Math.max(
                        obstacleHeight,
                        this.getObstacleHeight(blockAccess, x, currentPoint.y, z)
                );
            }
        }
        return obstacleHeight;
    }

    private PathPoint findLevelLeapLanding(PathPoint currentPoint, EnumFacing facing, float maxPathDistance) {
        PathPoint landing = this.findObstacleLeapLanding(currentPoint, facing, maxPathDistance);
        if (landing == null && this.waterPathingAllowed) {
            landing = this.findWaterLeapLanding(currentPoint, facing, maxPathDistance);
        }
        return landing != null ? landing : this.findGapLeapLanding(currentPoint, facing, maxPathDistance);
    }

    @Nullable
    private PathPoint findWaterLeapLanding(PathPoint currentPoint, EnumFacing facing, float maxPathDistance) {
        int maximumDistance = this.getMaximumLeapDistance(maxPathDistance);
        boolean foundWater = false;

        for (int distance = 1; distance <= maximumDistance; distance++) {
            int x = currentPoint.x + facing.getXOffset() * distance;
            int z = currentPoint.z + facing.getZOffset() * distance;
            if (this.isWaterSurfaceNode(x, currentPoint.y, z)) {
                foundWater = true;
                continue;
            }
            if (!foundWater) return null;

            PathNodeType landingType = this.getNodeType(x, currentPoint.y, z);
            float priority = this.creature.getPathPriority(landingType);
            if (landingType != PathNodeType.WALKABLE || priority < 0F
                    || !this.hasFullLandingSupport(x, currentPoint.y, z)) {
                return null;
            }

            PathPoint landing = this.openPoint(x, currentPoint.y, z);
            //Only water leaps are rejected during path construction. Ordinary leap edges must
            //reach RiftCreatureLeapHelper so a blocked ceiling records unable-to-path state.
            if (!this.hasClearLeapArc(currentPoint, landing)) return null;
            landing.nodeType = landingType;
            landing.costMalus = Math.max(landing.costMalus, priority + distance);
            return landing;
        }
        return null;
    }

    public boolean hasClearLeapArc(@NotNull PathPoint start, @NotNull PathPoint landing) {
        double nodeCenterOffsetX = this.entitySizeX * 0.5D;
        double nodeCenterOffsetZ = this.entitySizeZ * 0.5D;
        double startX = start.x + nodeCenterOffsetX;
        double startZ = start.z + nodeCenterOffsetZ;
        double landingX = landing.x + nodeCenterOffsetX;
        double landingZ = landing.z + nodeCenterOffsetZ;

        AxisAlignedBB creatureBounds = this.creature.getEntityBoundingBox();
        AxisAlignedBB startBounds = creatureBounds.offset(
                startX - this.creature.posX,
                start.y - creatureBounds.minY,
                startZ - this.creature.posZ
        );
        RiftCreatureLeapHelper leapHelper = this.creature.getCreatureMoveHelper().getLeapHelper();
        double targetHeight = Math.max(0D, landing.y - start.y);
        int directionX = Integer.compare(landing.x, start.x);
        int directionZ = Integer.compare(landing.z, start.z);
        EnumFacing facing = directionX > 0 ? EnumFacing.EAST
                : directionX < 0 ? EnumFacing.WEST
                : directionZ > 0 ? EnumFacing.SOUTH
                : EnumFacing.NORTH;
        double obstacleHeight = Math.max(
                Math.max(targetHeight, this.getLeadingObstacleHeight(start, facing, this.creature.world)),
                leapHelper.getPlannedObstacleClearance(
                        startBounds, landingX, landingZ,
                        this.creature.getNavigationBuilder().getLeapHeight()
                )
        );
        return obstacleHeight <= this.creature.getNavigationBuilder().getLeapHeight() + 1E-3D
                && leapHelper.hasClearPlannedLeap(
                        startBounds,
                        landingX,
                        landing.y,
                        landingZ,
                        obstacleHeight
                );
    }

    @Nullable
    private PathPoint findObstacleLeapLanding(PathPoint currentPoint, EnumFacing facing, float maxPathDistance) {
        int obstacleX = currentPoint.x + facing.getXOffset();
        int obstacleZ = currentPoint.z + facing.getZOffset();
        double obstacleHeight = this.getObstacleHeight(obstacleX, currentPoint.y, obstacleZ);
        if (obstacleHeight <= RiftCreatureMoveHelper.STANDARD_JUMP_CLEARANCE + 1E-3D
                || obstacleHeight > this.creature.getNavigationBuilder().getLeapHeight()) {
            return null;
        }
        AxisAlignedBB overheadClearance = new AxisAlignedBB(
                obstacleX,
                currentPoint.y + obstacleHeight + 1E-3D,
                obstacleZ,
                obstacleX + 1D,
                currentPoint.y + obstacleHeight + this.creature.height,
                obstacleZ + 1D
        );
        if (this.creature.world.collidesWithAnyBlock(overheadClearance)) return null;

        int maximumDistance = this.getMaximumLeapDistance(maxPathDistance);
        for (int distance = 2; distance <= maximumDistance; distance++) {
            int landingX = currentPoint.x + facing.getXOffset() * distance;
            int landingZ = currentPoint.z + facing.getZOffset() * distance;
            PathPoint landing = this.getWalkableLanding(
                    landingX, currentPoint.y, landingZ, (float)obstacleHeight + distance, false
            );
            if (landing != null) return landing;
        }
        return null;
    }

    @Nullable
    private PathPoint findGapLeapLanding(PathPoint currentPoint, EnumFacing facing, float maxPathDistance) {
        int maximumDistance = this.getMaximumLeapDistance(maxPathDistance);
        boolean foundGap = false;

        for (int distance = 1; distance <= maximumDistance; distance++) {
            int x = currentPoint.x + facing.getXOffset() * distance;
            int z = currentPoint.z + facing.getZOffset() * distance;
            if (this.isWaterSurfaceNode(x, currentPoint.y, z)) return null;
            PathNodeType columnType = this.getPathNodeType(this.blockaccess, x, currentPoint.y, z);

            if (columnType == PathNodeType.OPEN) {
                foundGap = true;
                continue;
            }
            if (!foundGap || columnType != PathNodeType.WALKABLE) return null;

            PathPoint landing = this.getWalkableLanding(x, currentPoint.y, z, distance, false);
            if (landing != null) return landing;
        }
        return null;
    }

    private int getMaximumLeapDistance(float maxPathDistance) {
        return (int)Math.floor(Math.min(this.creature.getNavigationBuilder().getLeapDistance(), maxPathDistance));
    }

    private double getObstacleHeight(int x, int baseY, int z) {
        return this.getObstacleHeight(this.blockaccess, x, baseY, z);
    }

    private double getObstacleHeight(IBlockAccess blockAccess, int x, int baseY, int z) {
        double top = baseY;
        int maximumY = baseY + (int)Math.ceil(this.creature.getNavigationBuilder().getLeapHeight());
        for (int y = baseY; y <= maximumY; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            AxisAlignedBB collisionBox = blockAccess.getBlockState(pos).getCollisionBoundingBox(blockAccess, pos);
            if (collisionBox != null) top = Math.max(top, y + collisionBox.maxY);
        }
        return top - baseY;
    }

    private PathNodeType getNodeType(int x, int y, int z) {
        return this.getPathNodeType(
                this.blockaccess,
                x, y, z,
                this.entity,
                this.entitySizeX,
                this.entitySizeY,
                this.entitySizeZ,
                this.getCanOpenDoors(),
                this.getCanEnterDoors()
        );
    }

    public void setWaterPathingAllowed(boolean waterPathingAllowed) {
        this.waterPathingAllowed = waterPathingAllowed;
    }

    private int findWaterSurfaceY(int x, int referenceY, int z) {
        int minimumY = Math.max(0, referenceY - WATER_SURFACE_SEARCH_DEPTH);
        int maximumY = Math.min(this.creature.world.getActualHeight() - 1, referenceY + 1);
        for (int y = maximumY; y >= minimumY; y--) {
            if (this.blockaccess.getBlockState(new BlockPos(x, y, z)).getMaterial() != Material.WATER) continue;

            int surfaceY = y + 1;
            while (surfaceY < this.creature.world.getActualHeight()
                    && this.blockaccess.getBlockState(new BlockPos(x, surfaceY, z)).getMaterial() == Material.WATER) {
                surfaceY++;
            }
            BlockPos surface = new BlockPos(x, surfaceY, z);
            if (this.blockaccess.getBlockState(surface).getMaterial() != Material.AIR) return Integer.MIN_VALUE;
            return surfaceY;
        }
        return Integer.MIN_VALUE;
    }

    private boolean isWaterSurface(IBlockAccess blockAccess, int x, int y, int z) {
        if (y <= 0) return false;
        BlockPos surface = new BlockPos(x, y, z);
        return blockAccess.getBlockState(surface).getMaterial() == Material.AIR
                && blockAccess.getBlockState(surface.down()).getMaterial() == Material.WATER;
    }

    private boolean isWaterSurfaceNode(int x, int y, int z) {
        return this.isWaterSurface(
                this.blockaccess,
                x + this.entitySizeX / 2,
                y,
                z + this.entitySizeZ / 2
        );
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean contains(@NotNull PathPoint[] points, int count, PathPoint candidate) {
        for (int index = 0; index < count; index++) {
            if (candidate.equals(points[index])) return true;
        }
        return false;
    }

    private static final class LeapAwarePathPoint extends PathPoint {
        private final Object logicalIdentity;
        private final Set<Object> leapPredecessorIdentities = new HashSet<>();

        private LeapAwarePathPoint(int x, int y, int z) {
            this(x, y, z, new Object());
        }

        private LeapAwarePathPoint(int x, int y, int z, @NotNull Object logicalIdentity) {
            super(x, y, z);
            this.logicalIdentity = logicalIdentity;
        }

        private void addLeapPredecessor(PathPoint predecessor) {
            this.leapPredecessorIdentities.add(this.logicalIdentity(predecessor));
        }

        private boolean hasLeapPredecessor(PathPoint predecessor) {
            return this.leapPredecessorIdentities.contains(this.logicalIdentity(predecessor));
        }

        @NotNull
        private Object logicalIdentity(PathPoint pathPoint) {
            return pathPoint instanceof LeapAwarePathPoint leapAwarePathPoint
                    ? leapAwarePathPoint.logicalIdentity
                    : pathPoint;
        }

        @Override
        public PathPoint cloneMove(int x, int y, int z) {
            LeapAwarePathPoint pathPoint = new LeapAwarePathPoint(x, y, z, this.logicalIdentity);
            pathPoint.index = this.index;
            pathPoint.totalPathDistance = this.totalPathDistance;
            pathPoint.distanceToNext = this.distanceToNext;
            pathPoint.distanceToTarget = this.distanceToTarget;
            pathPoint.previous = this.previous;
            pathPoint.visited = this.visited;
            pathPoint.distanceFromOrigin = this.distanceFromOrigin;
            pathPoint.cost = this.cost;
            pathPoint.costMalus = this.costMalus;
            pathPoint.nodeType = this.nodeType;
            pathPoint.leapPredecessorIdentities.addAll(this.leapPredecessorIdentities);
            return pathPoint;
        }
    }
}
