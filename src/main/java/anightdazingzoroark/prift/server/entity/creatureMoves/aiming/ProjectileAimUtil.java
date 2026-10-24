package anightdazingzoroark.prift.server.entity.creatureMoves.aiming;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

/**
 * Aim RiftLibrary's projectiles using their discrete air drag and gravity.
 * */
public class ProjectileAimUtil {
    private static final double AIR_DRAG = 0.99f;
    private static final double GRAVITY = 0.05f;

    @NotNull
    public static Vec3d getLaunchVelocity(@NotNull Vec3d origin, @NotNull Vec3d target, double speed) {
        Vec3d offset = target.subtract(origin);
        if (speed <= 0D || offset.lengthSquared() < 1E-12D) return Vec3d.ZERO;

        //find the first flight time compatible with the launch speed, choosing the lower arc
        for (int tick = 1; tick <= 200; tick++) {
            if (getRequiredVelocity(offset, tick).lengthSquared() > speed * speed) continue;
            double lowerTime = tick - 1D;
            double upperTime = tick;
            for (int step = 0; step < 32; step++) {
                double time = (lowerTime + upperTime) * 0.5D;
                if (getRequiredVelocity(offset, time).lengthSquared() > speed * speed) lowerTime = time;
                else upperTime = time;
            }
            return getRequiredVelocity(offset, upperTime).normalize().scale(speed);
        }

        //unreachable points retain a direct shot instead of an invalid trajectory
        return offset.normalize().scale(speed);
    }

    @NotNull
    private static Vec3d getRequiredVelocity(@NotNull Vec3d offset, double time) {
        int ticks = (int) Math.floor(time);
        double decay = Math.pow(AIR_DRAG, ticks);
        double travel = (1D - decay) / (1D - AIR_DRAG) + (time - ticks) * decay;
        double gravityDrop = GRAVITY * (time - travel) / (1D - AIR_DRAG);
        return new Vec3d(offset.x / travel, (offset.y + gravityDrop) / travel, offset.z / travel);
    }
}