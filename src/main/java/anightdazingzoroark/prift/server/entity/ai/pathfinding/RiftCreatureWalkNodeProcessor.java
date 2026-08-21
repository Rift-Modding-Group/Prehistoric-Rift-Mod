package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
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

public class RiftCreatureWalkNodeProcessor extends WalkNodeProcessor {
    private static final int WATER_SURFACE_SEARCH_DEPTH = 4;
    private static final float WATER_PATH_COST = 2F;
    private static final double LEAP_ARC_SCAN_STEP = 0.25D;

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
        if (!this.allowLeaps
                || !this.creature.getNavigationBuilder().getCanLeap()
                || this.creature.bodyTouchingLiquid()
                || currentPoint.nodeType == PathNodeType.WATER
                || this.isWaterSurfaceNode(currentPoint.x, currentPoint.y, currentPoint.z)) {
            return count;
        }

        int walkingOptionCount = count;

        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
            //a leap is a fallback edge. prefer every valid walking edge, including a survivable
            //drop, so creatures walk off safe ledges instead of leaping over them.
            if (this.containsWalkingOption(pathOptions, walkingOptionCount, currentPoint, facing)) continue;

            count = this.addLeapOption(pathOptions, count,
                    this.findUpwardLeapLanding(currentPoint, facing, maxDistance), targetPoint, maxDistance);
            count = this.addLeapOption(pathOptions, count,
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

    private int addLeapOption(PathPoint[] pathOptions, int count, @Nullable PathPoint leapPoint, PathPoint targetPoint, float maxDistance) {
        if (leapPoint != null && !leapPoint.visited && leapPoint.distanceTo(targetPoint) < maxDistance && !this.contains(pathOptions, count, leapPoint)) {
            pathOptions[count++] = leapPoint;
        }
        return count;
    }

    @Nullable
    private PathPoint findUpwardLeapLanding(PathPoint currentPoint, EnumFacing facing, float maxPathDistance) {
        double ledgeHeight = this.getLeadingObstacleHeight(currentPoint, facing);
        if (ledgeHeight <= RiftCreatureMoveHelper.STANDARD_JUMP_HEIGHT + 1E-3D || ledgeHeight > this.creature.getNavigationBuilder().getLeapHeight() + 1E-3D) {
            return null;
        }

        int landingY = currentPoint.y + (int)Math.ceil(ledgeHeight - 1E-3D);
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
        int offsetX = facing.getXOffset();
        int offsetZ = facing.getZOffset();
        int minimumX = currentPoint.x + (offsetX > 0 ? this.entitySizeX : offsetX);
        int maximumX = minimumX + (offsetX == 0 ? this.entitySizeX - 1 : 0);
        int minimumZ = currentPoint.z + (offsetZ > 0 ? this.entitySizeZ : offsetZ);
        int maximumZ = minimumZ + (offsetZ == 0 ? this.entitySizeZ - 1 : 0);

        double obstacleHeight = 0D;
        for (int x = minimumX; x <= maximumX; x++) {
            for (int z = minimumZ; z <= maximumZ; z++) {
                obstacleHeight = Math.max(obstacleHeight, this.getObstacleHeight(x, currentPoint.y, z));
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

    private boolean hasClearLeapArc(PathPoint start, PathPoint landing) {
        double nodeCenterOffsetX = this.entitySizeX * 0.5D;
        double nodeCenterOffsetZ = this.entitySizeZ * 0.5D;
        double startX = start.x + nodeCenterOffsetX;
        double startZ = start.z + nodeCenterOffsetZ;
        double displacementX = landing.x - start.x;
        double displacementZ = landing.z - start.z;
        int samples = Math.max(2, (int)Math.ceil(
                Math.sqrt(displacementX * displacementX + displacementZ * displacementZ) / LEAP_ARC_SCAN_STEP
        ));

        AxisAlignedBB creatureBounds = this.creature.getEntityBoundingBox();
        AxisAlignedBB startBounds = creatureBounds.offset(
                startX - this.creature.posX,
                start.y - creatureBounds.minY + 1E-3D,
                startZ - this.creature.posZ
        );
        double landingHeight = landing.y - start.y;
        double arcHeight = this.creature.getNavigationBuilder().getLeapHeight() + 0.25D;

        for (int sample = 1; sample < samples; sample++) {
            double progress = (double)sample / samples;
            double verticalOffset = landingHeight * progress + 4D * arcHeight * progress * (1D - progress);
            AxisAlignedBB sampleBounds = startBounds.offset(displacementX * progress, verticalOffset, displacementZ * progress);
            if (this.creature.world.collidesWithAnyBlock(sampleBounds)) return false;
        }
        return true;
    }

    @Nullable
    private PathPoint findObstacleLeapLanding(PathPoint currentPoint, EnumFacing facing, float maxPathDistance) {
        int obstacleX = currentPoint.x + facing.getXOffset();
        int obstacleZ = currentPoint.z + facing.getZOffset();
        double obstacleHeight = this.getObstacleHeight(obstacleX, currentPoint.y, obstacleZ);
        if (obstacleHeight <= 1D || obstacleHeight > this.creature.getNavigationBuilder().getLeapHeight()) return null;
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
        double top = baseY;
        int maximumY = baseY + (int)Math.ceil(this.creature.getNavigationBuilder().getLeapHeight());
        for (int y = baseY; y <= maximumY; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            AxisAlignedBB collisionBox = this.blockaccess.getBlockState(pos).getCollisionBoundingBox(this.blockaccess, pos);
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
}
