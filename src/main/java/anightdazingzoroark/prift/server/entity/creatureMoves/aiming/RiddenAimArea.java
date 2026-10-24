package anightdazingzoroark.prift.server.entity.creatureMoves.aiming;

import anightdazingzoroark.prift.api.util.MathUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A perspective ellipse shared by the rendered reticle and server-side melee selection.
 * */
@SuppressWarnings("SuspiciousNameCombination")
public record RiddenAimArea(
        @NotNull Vec3d origin, @NotNull Vec3d forward,
        double horizontalSlope, double verticalSlope
) {
    //screen radii are fractions of screen height, independent of GUI scale and aspect ratio.
    public static final double HORIZONTAL_SCREEN_RADIUS = 0.06D;
    public static final double VERTICAL_SCREEN_RADIUS = 0.02D;

    @Nullable
    public Vec3d getHitPosition(@NotNull AxisAlignedBB bounds) {
        if (horizontalSlope <= 0D || horizontalSlope > 1D || verticalSlope <= 0D || verticalSlope > 1D
                || forward.lengthSquared() <= 0.99D || forward.lengthSquared() >= 1.01D
        ) {
            return null;
        }
        RayTraceResult centralHit = bounds.calculateIntercept(origin, origin.add(forward.scale(128D)));
        if (centralHit != null) return centralHit.hitVec;
        Vec3d right = forward.crossProduct(new Vec3d(0D, 1D, 0D)).normalize();
        Vec3d up = right.crossProduct(forward).normalize();
        Vec3d[] corners = new Vec3d[8];
        double[] depths = new double[8];
        double[] projectedX = new double[8];
        double[] projectedY = new double[8];
        for (int index = 0; index < 8; index++) {
            corners[index] = new Vec3d(
                    (index & 1) == 0 ? bounds.minX : bounds.maxX,
                    (index & 2) == 0 ? bounds.minY : bounds.maxY,
                    (index & 4) == 0 ? bounds.minZ : bounds.maxZ
            );
            Vec3d offset = corners[index].subtract(origin);
            depths[index] = offset.dotProduct(forward);
            if (depths[index] <= 0.01D) continue;
            projectedX[index] = offset.dotProduct(right) / (depths[index] * horizontalSlope);
            projectedY[index] = offset.dotProduct(up) / (depths[index] * verticalSlope);
        }
        //projected box edges bound a convex silhouette. Checking every corner pair also
        //handles faces viewed edge-on, without an axis-aligned screen rectangle approximation.
        double nearestDistance = 1D;
        Vec3d nearestPosition = null;
        for (int first = 0; first < 8; first++) {
            if (depths[first] <= 0.01D) continue;
            for (int second = first; second < 8; second++) {
                if (depths[second] <= 0.01D) continue;
                double deltaX = projectedX[second] - projectedX[first];
                double deltaY = projectedY[second] - projectedY[first];
                double lengthSquared = deltaX * deltaX + deltaY * deltaY;
                double fraction = 0D;
                if (lengthSquared > 1E-12D) {
                    fraction = Math.clamp(-(projectedX[first] * deltaX + projectedY[first] * deltaY) / lengthSquared, 0D, 1D);
                }
                double x = MathUtil.slopeResult(fraction, true, 0D, 1D, projectedX[first], projectedX[second]);
                double y = MathUtil.slopeResult(fraction, true, 0D, 1D, projectedY[first], projectedY[second]);
                double distance = x * x + y * y;
                if (distance > nearestDistance) continue;
                nearestDistance = distance;
                // Undo perspective division to recover a point inside the world-space box.
                double worldFraction = fraction * depths[first] / ((1D - fraction) * depths[second] + fraction * depths[first]);
                nearestPosition = new Vec3d(
                        MathUtil.slopeResult(worldFraction, true, 0D, 1D, corners[first].x, corners[second].x),
                        MathUtil.slopeResult(worldFraction, true, 0D, 1D, corners[first].y, corners[second].y),
                        MathUtil.slopeResult(worldFraction, true, 0D, 1D, corners[first].z, corners[second].z));
            }
        }
        return nearestPosition;
    }
}