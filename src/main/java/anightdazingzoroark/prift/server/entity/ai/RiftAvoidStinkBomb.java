package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.properties.OtherEntityProperties;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RiftAvoidStinkBomb extends EntityAIBase {
    private static final double FLEE_DISTANCE = 24D;
    private static final double FLEE_DISTANCE_SQ = FLEE_DISTANCE * FLEE_DISTANCE;
    private static final int PATH_RETRY_INTERVAL = 10;
    @NotNull
    private final EntityLiving entity;
    @NotNull
    private final PathNavigate navigation;
    @Nullable
    private Path path;
    private int pathRetryTicks;

    public RiftAvoidStinkBomb(@NotNull EntityLiving entity) {
        this.entity = entity;
        this.navigation = entity.getNavigator();
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        OtherEntityProperties otherEntityProperties = OtherEntityProperties.get(this.entity);
        if (otherEntityProperties == null || !otherEntityProperties.isStinkBombed()) return false;
        Vec3d origin = otherEntityProperties.getStinkBombOrigin();
        if (this.entity.getDistanceSq(origin.x, origin.y, origin.z) >= FLEE_DISTANCE_SQ) {
            otherEntityProperties.clearStinkBomb();
            return false;
        }

        this.clearTargets();
        this.path = this.findPathAway(origin);
        this.pathRetryTicks = PATH_RETRY_INTERVAL;
        return true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        OtherEntityProperties otherEntityProperties = OtherEntityProperties.get(this.entity);
        if (otherEntityProperties == null || !otherEntityProperties.isStinkBombed()) return false;

        Vec3d origin = otherEntityProperties.getStinkBombOrigin();
        if (this.entity.getDistanceSq(origin.x, origin.y, origin.z) < FLEE_DISTANCE_SQ) return true;
        otherEntityProperties.clearStinkBomb();
        return false;
    }

    @Override
    public void startExecuting() {
        if (this.path != null) this.navigation.setPath(this.path, 1D);
    }

    @Override
    public void resetTask() {
        this.navigation.clearPath();
        this.path = null;
        this.pathRetryTicks = 0;
    }

    @Override
    public void updateTask() {
        this.clearTargets();
        if (!this.navigation.noPath()) return;
        if (this.pathRetryTicks > 0) {
            this.pathRetryTicks--;
            return;
        }

        OtherEntityProperties otherEntityProperties = OtherEntityProperties.get(this.entity);
        if (otherEntityProperties == null) return;
        this.path = this.findPathAway(otherEntityProperties.getStinkBombOrigin());
        this.pathRetryTicks = PATH_RETRY_INTERVAL;
        if (this.path != null) this.navigation.setPath(this.path, 1D);
    }

    private void clearTargets() {
        this.entity.setAttackTarget(null);
        this.entity.setRevengeTarget(null);
    }

    @Nullable
    private Path findPathAway(@NotNull Vec3d origin) {
        if (this.entity instanceof EntityCreature entityCreature) {
            Vec3d target = RandomPositionGenerator.findRandomTargetBlockAwayFrom(entityCreature, 16, 7, origin);
            if (target == null || target.squareDistanceTo(origin) <= this.entity.getDistanceSq(origin.x, origin.y, origin.z)) return null;
            return this.navigation.getPathToXYZ(target.x, target.y, target.z);
        }

        double awayX = this.entity.posX - origin.x;
        double awayZ = this.entity.posZ - origin.z;
        double horizontalDistance = Math.sqrt(awayX * awayX + awayZ * awayZ);
        if (horizontalDistance < 0.001D) {
            double angle = this.entity.getRNG().nextDouble() * Math.PI * 2D;
            awayX = Math.cos(angle);
            awayZ = Math.sin(angle);
        }
        else {
            awayX /= horizontalDistance;
            awayZ /= horizontalDistance;
        }

        for (int attempt = 0; attempt < 10; attempt++) {
            double lateralOffset = (this.entity.getRNG().nextDouble() - 0.5D) * 8D;
            double targetX = this.entity.posX + awayX * FLEE_DISTANCE - awayZ * lateralOffset;
            double targetY = this.entity.posY + this.entity.getRNG().nextInt(7) - 3;
            double targetZ = this.entity.posZ + awayZ * FLEE_DISTANCE + awayX * lateralOffset;
            Path possiblePath = this.navigation.getPathToXYZ(targetX, targetY, targetZ);
            if (possiblePath != null) return possiblePath;
        }
        return null;
    }
}
