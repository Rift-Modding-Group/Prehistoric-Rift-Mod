package anightdazingzoroark.prift.api.creature.builder.detectionRule;

import anightdazingzoroark.prift.api.creature.ICreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

/**
 * Measures along the line between hitbox centers, excluding the portion inside the user's hitbox.
 * The target's hitbox size is not subtracted. A target center inside the user has zero distance.
 */
public class HitboxDistanceFromUserDetectionRule extends DetectionRule {
    private final double minimumDistanceSquared;
    private final double maximumDistanceSquared;

    /**
     * Require at least the given distance, with no upper limit.
     */
    public HitboxDistanceFromUserDetectionRule(double minimumDistance) {
        this(minimumDistance, -1D);
    }

    /**
     * Require a distance within the inclusive bounds. A negative maximum disables the upper bound.
     */
    public HitboxDistanceFromUserDetectionRule(double minimumDistance, double maximumDistance) {
        if (minimumDistance < 0D || maximumDistance >= 0D && maximumDistance < minimumDistance) {
            throw new IllegalArgumentException("Minimum distance must be nonnegative and cannot exceed a nonnegative maximum!");
        }
        this.minimumDistanceSquared = minimumDistance * minimumDistance;
        this.maximumDistanceSquared = maximumDistance < 0D ? -1D : maximumDistance * maximumDistance;
    }

    @Override
    public boolean targetWithinRange(@NotNull ICreature user, @NotNull EntityLivingBase target) {
        AxisAlignedBB userBounds = user.getEntityBoundingBox();
        Vec3d offset = target.getEntityBoundingBox().getCenter().subtract(userBounds.getCenter());
        double insideFraction = 1D;
        if (offset.x != 0D) {
            insideFraction = Math.min(insideFraction, (userBounds.maxX - userBounds.minX) / (2D * Math.abs(offset.x)));
        }
        if (offset.y != 0D) {
            insideFraction = Math.min(insideFraction, (userBounds.maxY - userBounds.minY) / (2D * Math.abs(offset.y)));
        }
        if (offset.z != 0D) {
            insideFraction = Math.min(insideFraction, (userBounds.maxZ - userBounds.minZ) / (2D * Math.abs(offset.z)));
        }
        double distanceSquared = offset.subtract(offset.scale(insideFraction)).lengthSquared();
        return distanceSquared >= this.minimumDistanceSquared
                && (this.maximumDistanceSquared < 0D || distanceSquared <= this.maximumDistanceSquared);
    }
}
