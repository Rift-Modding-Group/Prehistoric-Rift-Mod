package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * A path whose endpoint is an attack position, rather than the target entity's position.
 * */
public class AttackApproachPath extends Path {
    private static final int MAX_PATH_ATTEMPTS = 4;

    public AttackApproachPath(@NotNull Path path) {
        super(IntStream.range(0, path.getCurrentPathLength()).mapToObj(path::getPathPointFromIndex).toArray(PathPoint[]::new));
        this.setCurrentPathIndex(path.getCurrentPathIndex());
    }

    /**
     * Plan an approach using the same captured animation poses as range detection.
     */
    @Nullable
    public static AttackApproachPath findPath(
            @NotNull RiftCreature creature, @NotNull EntityLivingBase target, @NotNull AnimatedHitboxDetectionRule detection
    ) {
        if (!target.isEntityAlive() || !creature.isOnGround() || creature.bodyTouchingLiquid()
                || !creature.getCurrentMove().isEmpty()) return null;

        AnimatedHitboxDetectionRule.AttackPreview preview = detection.createPreview(creature, target);
        if (preview == null) return null;
        float yaw = CreatureLookUtil.getLookBasedAttackYaw(creature, target);
        return findPath(
                creature, target, preview.bounds(Vec3d.ZERO, yaw),
                position -> preview.bounds(position, CreatureLookUtil.getLookBasedAttackYaw(creature, target, position)),
                AnimatedHitboxDetectionRule.getTargetBounds(target)
        );
    }

    /**
     * Find a supported position on the creature's current level where the actual attack can connect.
     * */
    @Nullable
    public static AttackApproachPath findPath(
            @NotNull EntityLiving creature, @NotNull EntityLivingBase target, @NotNull List<AxisAlignedBB> relativeHitboxes,
            @NotNull Function<Vec3d, List<AxisAlignedBB>> hitboxesAtPosition, @NotNull List<AxisAlignedBB> targetBounds
    ) {
        double maximumReach = 0D;
        for (AxisAlignedBB bounds : relativeHitboxes) {
            double x = Math.max(Math.abs(bounds.minX), Math.abs(bounds.maxX));
            double z = Math.max(Math.abs(bounds.minZ), Math.abs(bounds.maxZ));
            maximumReach = Math.max(maximumReach, Math.sqrt(x * x + z * z));
        }
        maximumReach += target.width * 0.5D;

        Vec3d origin = creature.getPositionVector();
        Vec3d away = new Vec3d(origin.x - target.posX, 0D, origin.z - target.posZ);
        if (away.lengthSquared() < 1E-4D) {
            double yaw = Math.toRadians(creature.rotationYaw);
            away = new Vec3d(Math.sin(yaw), 0D, -Math.cos(yaw));
        }
        else away = away.normalize();

        // Use the navigator's node centers so it can actually finish at the predicted position.
        double nodeOffset = MathHelper.floor(creature.width + 1F) * 0.5D;
        Set<Vec3d> candidates = new HashSet<>();
        for (double distance = 0.5D; distance <= maximumReach; distance += 0.5D) {
            candidates.add(new Vec3d(
                    MathHelper.floor(target.posX + away.x * distance - nodeOffset) + nodeOffset,
                    creature.getEntityBoundingBox().minY,
                    MathHelper.floor(target.posZ + away.z * distance - nodeOffset) + nodeOffset
            ));
        }

        int pathAttempts = 0;
        for (Vec3d position : candidates.stream().sorted(Comparator.comparingDouble(origin::squareDistanceTo)).toList()) {
            AxisAlignedBB body = creature.getEntityBoundingBox().offset(position.subtract(origin));
            if (body.intersects(target.getEntityBoundingBox())
                    || !creature.world.getCollisionBoxes(creature, body).isEmpty()
                    || creature.world.getCollisionBoxes(creature, body.offset(0D, -0.125D, 0D)).isEmpty()) continue;
            List<AxisAlignedBB> volumes = hitboxesAtPosition.apply(position);
            if (volumes.stream().noneMatch(volume -> targetBounds.stream().anyMatch(volume::intersects))) continue;

            if (pathAttempts++ >= MAX_PATH_ATTEMPTS) break;
            Path path = creature.getNavigator().getPathToXYZ(position.x, position.y, position.z);
            if (path == null || path.getCurrentPathLength() == 0) continue;
            Vec3d end = path.getVectorFromIndex(creature, path.getCurrentPathLength() - 1);
            double x = end.x - position.x;
            double z = end.z - position.z;
            if (x * x + z * z > 0.0625D || Math.abs(end.y - position.y) > 1D) continue;

            boolean safePath = true;
            for (int index = path.getCurrentPathIndex(); index < path.getCurrentPathLength(); index++) {
                PathNodeType type = path.getPathPointFromIndex(index).nodeType;
                if (creature.getPathPriority(type) < 0f || switch (type) {
                    case DANGER_FIRE, DAMAGE_FIRE, DANGER_CACTUS, DAMAGE_CACTUS, DANGER_OTHER, DAMAGE_OTHER, LAVA, WATER -> true;
                    default -> false;
                }) {
                    safePath = false;
                    break;
                }
            }
            if (safePath) return new AttackApproachPath(path);
        }
        return null;
    }
}
