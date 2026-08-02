package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.state.IBlockState;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class RiftCreatureWalkNodeProcessor extends WalkNodeProcessor {
    private final RiftCreature creature;

    public RiftCreatureWalkNodeProcessor(RiftCreature creature) {
        this.creature = creature;
    }

    @Override
    public int findPathOptions(PathPoint[] pathOptions, PathPoint currentPoint, PathPoint targetPoint, float maxDistance) {
        int count = super.findPathOptions(pathOptions, currentPoint, targetPoint, maxDistance);
        if (!this.creature.getNavigation().getCanLeap()) return count;

        int walkingOptionCount = count;

        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
            //A leap is a fallback edge. Adding one beside a valid walking edge can make a wide
            //creature interpret the successive levels of a staircase as one tall ledge.
            if (this.containsWalkingOption(pathOptions, walkingOptionCount, currentPoint, facing)) continue;

            count = this.addLeapOption(
                    pathOptions,
                    count,
                    this.findUpwardLeapLanding(currentPoint, facing, maxDistance),
                    targetPoint,
                    maxDistance
            );
            count = this.addLeapOption(
                    pathOptions,
                    count,
                    this.findLevelLeapLanding(currentPoint, facing, maxDistance),
                    targetPoint,
                    maxDistance
            );
        }
        return count;
    }

    private boolean containsWalkingOption(PathPoint[] points, int count, PathPoint currentPoint, EnumFacing facing) {
        int adjacentX = currentPoint.x + facing.getXOffset();
        int adjacentZ = currentPoint.z + facing.getZOffset();
        for (int index = 0; index < count; index++) {
            PathPoint point = points[index];
            if (point.x == adjacentX && point.z == adjacentZ) return true;
        }
        return false;
    }

    private int addLeapOption(PathPoint[] pathOptions, int count, @Nullable PathPoint leapPoint,
            PathPoint targetPoint, float maxDistance) {
        if (leapPoint != null && !leapPoint.visited
                && leapPoint.distanceTo(targetPoint) < maxDistance
                && !this.contains(pathOptions, count, leapPoint)) {
            pathOptions[count++] = leapPoint;
        }
        return count;
    }

    @Nullable
    private PathPoint findUpwardLeapLanding(PathPoint currentPoint, EnumFacing facing, float maxPathDistance) {
        double ledgeHeight = this.getLeadingObstacleHeight(currentPoint, facing);
        if (ledgeHeight <= RiftCreatureMoveHelper.STANDARD_JUMP_HEIGHT + 1E-3D || ledgeHeight > this.creature.getNavigation().getLeapHeight() + 1E-3D) {
            return null;
        }

        int landingY = currentPoint.y + (int)Math.ceil(ledgeHeight - 1E-3D);
        int maximumDistance = (int)Math.floor(Math.min(
                this.creature.getNavigation().getLeapDistance(),
                maxPathDistance
        ));
        for (int distance = 1; distance <= maximumDistance; distance++) {
            int landingX = currentPoint.x + facing.getXOffset() * distance;
            int landingZ = currentPoint.z + facing.getZOffset() * distance;
            PathNodeType landingType = this.getNodeType(landingX, landingY, landingZ);
            float priority = this.creature.getPathPriority(landingType);
            if (landingType != PathNodeType.WALKABLE || priority < 0F
                    || !this.hasFullLandingSupport(landingX, landingY, landingZ)) {
                continue;
            }

            PathPoint landing = this.openPoint(landingX, landingY, landingZ);
            landing.nodeType = landingType;
            landing.costMalus = Math.max(landing.costMalus, priority + (float)ledgeHeight + distance);
            return landing;
        }
        return null;
    }

    private boolean hasFullLandingSupport(int landingX, int landingY, int landingZ) {
        for (int x = landingX; x < landingX + this.entitySizeX; x++) {
            for (int z = landingZ; z < landingZ + this.entitySizeZ; z++) {
                if (this.getPathNodeType(this.blockaccess, x, landingY, z) != PathNodeType.WALKABLE) {
                    return false;
                }
            }
        }
        return true;
    }

    private double getLeadingObstacleHeight(PathPoint currentPoint, EnumFacing facing) {
        int candidateX = currentPoint.x + facing.getXOffset();
        int candidateZ = currentPoint.z + facing.getZOffset();
        int minimumX = candidateX;
        int maximumX = candidateX + this.entitySizeX - 1;
        int minimumZ = candidateZ;
        int maximumZ = candidateZ + this.entitySizeZ - 1;

        if (facing.getXOffset() > 0) minimumX = maximumX;
        else if (facing.getXOffset() < 0) maximumX = minimumX;
        if (facing.getZOffset() > 0) minimumZ = maximumZ;
        else if (facing.getZOffset() < 0) maximumZ = minimumZ;

        double obstacleHeight = 0D;
        for (int x = minimumX; x <= maximumX; x++) {
            for (int z = minimumZ; z <= maximumZ; z++) {
                obstacleHeight = Math.max(obstacleHeight, this.getObstacleHeight(x, currentPoint.y, z));
            }
        }
        return obstacleHeight;
    }

    private PathPoint findLevelLeapLanding(PathPoint currentPoint, EnumFacing facing, float maxPathDistance) {
        PathPoint obstacleLanding = this.findObstacleLeapLanding(currentPoint, facing, maxPathDistance);
        return obstacleLanding != null
                ? obstacleLanding
                : this.findGapLeapLanding(currentPoint, facing, maxPathDistance);
    }

    private PathPoint findObstacleLeapLanding(PathPoint currentPoint, EnumFacing facing, float maxPathDistance) {
        int obstacleX = currentPoint.x + facing.getXOffset();
        int obstacleZ = currentPoint.z + facing.getZOffset();
        double obstacleHeight = this.getObstacleHeight(obstacleX, currentPoint.y, obstacleZ);
        if (obstacleHeight <= 1D || obstacleHeight > this.creature.getNavigation().getLeapHeight()) return null;
        AxisAlignedBB overheadClearance = new AxisAlignedBB(
                obstacleX,
                currentPoint.y + obstacleHeight + 1E-3D,
                obstacleZ,
                obstacleX + 1D,
                currentPoint.y + obstacleHeight + this.creature.height,
                obstacleZ + 1D
        );
        if (this.creature.world.collidesWithAnyBlock(overheadClearance)) return null;

        int maximumDistance = (int)Math.floor(Math.min(
                this.creature.getNavigation().getLeapDistance(),
                maxPathDistance
        ));
        for (int distance = 2; distance <= maximumDistance; distance++) {
            int landingX = currentPoint.x + facing.getXOffset() * distance;
            int landingZ = currentPoint.z + facing.getZOffset() * distance;
            PathNodeType landingType = this.getNodeType(landingX, currentPoint.y, landingZ);
            float priority = this.creature.getPathPriority(landingType);
            if (landingType != PathNodeType.WALKABLE || priority < 0F) continue;

            PathPoint landing = this.openPoint(landingX, currentPoint.y, landingZ);
            landing.nodeType = landingType;
            landing.costMalus = Math.max(landing.costMalus, priority + (float)obstacleHeight + distance);
            return landing;
        }
        return null;
    }

    @Nullable
    private PathPoint findGapLeapLanding(PathPoint currentPoint, EnumFacing facing, float maxPathDistance) {
        int maximumDistance = (int)Math.floor(Math.min(
                this.creature.getNavigation().getLeapDistance(),
                maxPathDistance
        ));
        boolean foundGap = false;

        for (int distance = 1; distance <= maximumDistance; distance++) {
            int x = currentPoint.x + facing.getXOffset() * distance;
            int z = currentPoint.z + facing.getZOffset() * distance;
            PathNodeType columnType = this.getPathNodeType(this.blockaccess, x, currentPoint.y, z);

            if (columnType == PathNodeType.OPEN) {
                foundGap = true;
                continue;
            }
            if (!foundGap || columnType != PathNodeType.WALKABLE) return null;

            PathNodeType landingType = this.getNodeType(x, currentPoint.y, z);
            float priority = this.creature.getPathPriority(landingType);
            if (landingType != PathNodeType.WALKABLE || priority < 0F) continue;
            PathPoint landing = this.openPoint(x, currentPoint.y, z);
            landing.nodeType = landingType;
            landing.costMalus = Math.max(landing.costMalus, priority + distance);
            return landing;
        }
        return null;
    }

    private double getObstacleHeight(int x, int baseY, int z) {
        double top = baseY;
        int maximumY = baseY + (int)Math.ceil(this.creature.getNavigation().getLeapHeight());
        for (int y = baseY; y <= maximumY; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            IBlockState state = this.blockaccess.getBlockState(pos);
            AxisAlignedBB collisionBox = state.getCollisionBoundingBox(this.blockaccess, pos);
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

    private boolean contains(PathPoint[] points, int count, PathPoint candidate) {
        for (int index = 0; index < count; index++) {
            if (candidate.equals(points[index])) return true;
        }
        return false;
    }
}
