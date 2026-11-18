package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.api.creature.builder.CreatureNavigationBuilder;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a creature's wall climbing and collision queries for climbing paths.
 */
public class RiftCreatureClimbHelper {
    @NotNull
    private final RiftCreature creature;
    private boolean climbing;

    public RiftCreatureClimbHelper(@NotNull RiftCreature creature) {
        this.creature = creature;
    }

    public void updateClimbing(float strafe, float forward) {
        this.climbing = false;
        if (strafe == 0f && forward == 0f) return;

        CreatureNavigationBuilder navigation = this.creature.getNavigationBuilder();
        if (navigation == null || !navigation.getCanClimb() || this.creature.bodyTouchingLiquid()
                || this.creature.isLeaping() || this.creature.getIsSleeping() || this.creature.isStaggered()
                || !this.creature.getCurrentMove().isEmpty()
                || this.creature.isBeingRidden() && !this.creature.riderCanControl()
        ) {
            return;
        }

        double yaw = Math.toRadians(this.creature.rotationYaw);
        double directionX = strafe * Math.cos(yaw) - forward * Math.sin(yaw);
        double directionZ = forward * Math.cos(yaw) + strafe * Math.sin(yaw);
        this.climbing = this.touchesWall(this.creature.world, this.creature.getEntityBoundingBox(), directionX, directionZ);
        if (this.climbing) {
            this.creature.motionY = Math.max(this.creature.motionY, 0.2D);
            this.creature.fallDistance = 0f;
        }
    }

    public boolean isClimbing() {
        return this.climbing;
    }

    public boolean touchesWall(@NotNull IBlockAccess blocks, @NotNull AxisAlignedBB body, double x, double z) {
        double distance = Math.hypot(x, z);
        if (distance < 1E-4D) return false;

        AxisAlignedBB probe = body.grow(-1E-4D).expand(x / distance * 0.05D, 0D, z / distance * 0.05D);
        for (BlockPos position : BlockPos.getAllInBoxMutable(
                new BlockPos(probe.minX, probe.minY - 1D, probe.minZ),
                new BlockPos(probe.maxX, probe.maxY, probe.maxZ))
        ) {
            IBlockState state = blocks.getBlockState(position);
            if (!state.getMaterial().isSolid()) continue;
            AxisAlignedBB collision = state.getCollisionBoundingBox(blocks, position);
            if (collision != null && collision.offset(position).intersects(probe)) return true;
        }
        return false;
    }

    @NotNull
    private List<AxisAlignedBB> getCollisions(@NotNull IBlockAccess blocks, @NotNull AxisAlignedBB bounds) {
        List<AxisAlignedBB> collisions = new ArrayList<>();
        for (BlockPos position : BlockPos.getAllInBoxMutable(
                new BlockPos(bounds.minX, bounds.minY - 1D, bounds.minZ),
                new BlockPos(bounds.maxX, bounds.maxY, bounds.maxZ))
        ) {
            AxisAlignedBB collision = blocks.getBlockState(position).getCollisionBoundingBox(blocks, position);
            if (collision != null && collision.offset(position).intersects(bounds)) collisions.add(collision.offset(position));
        }
        return collisions;
    }

    /**
     * Find a clear ascent against a solid wall followed by a supported step over its top.
     */
    public int findClimbHeight(@NotNull IBlockAccess blocks, @NotNull AxisAlignedBB body, int x, int z, int maximumHeight) {
        List<AxisAlignedBB> obstacles = this.getCollisions(blocks, body.expand(x, 0D, z));
        double approachX = x;
        double approachZ = z;
        for (AxisAlignedBB obstacle : obstacles) {
            approachX = obstacle.calculateXOffset(body, approachX);
            approachZ = obstacle.calculateZOffset(body, approachZ);
        }
        if (approachX == x && approachZ == z) return 0;

        AxisAlignedBB wallBody = body.offset(approachX, 0D, approachZ).grow(-1E-4D, 0D, -1E-4D);
        for (int height = 1; height <= maximumHeight; height++) {
            if (!this.touchesWall(blocks, wallBody, x, z) || !this.getCollisions(blocks, wallBody.expand(0D, 1D, 0D)).isEmpty()) return 0;
            wallBody = wallBody.offset(0D, 1D, 0D);
            AxisAlignedBB landing = body.offset(x, height, z);
            if (!this.getCollisions(blocks, landing).isEmpty()) continue;
            if (!this.getCollisions(blocks, wallBody.expand(x - approachX, 0D, z - approachZ)).isEmpty()) return 0;
            if (!this.getCollisions(blocks, landing.offset(0D, -0.125D, 0D)).isEmpty()) return height;
            return 0;
        }
        return 0;
    }
}