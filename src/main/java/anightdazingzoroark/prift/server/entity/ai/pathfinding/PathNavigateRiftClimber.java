package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class PathNavigateRiftClimber extends PathNavigateGround {
    private BlockPos targetPosition;
    private final RiftCreature creature;
    private final double closeEnoughDistance = 1.0; // Distance within which entity considers it has arrived
    private final double minimumMovementThreshold = 0.1; // Minimum distance to move to prevent jittery motion
    private long lastPathCalculationTime = 0;
    private final long pathRecalculationCooldown = 1000; // Time in milliseconds between path recalculations

    public PathNavigateRiftClimber(RiftCreature creature, World worldIn) {
        super(creature, worldIn);
        this.creature = creature;
    }

    @Override
    public Path getPathToPos(BlockPos pos) {
        this.targetPosition = pos;
        return super.getPathToPos(pos);
    }

    @Override
    public Path getPathToEntityLiving(Entity entityIn) {
        this.targetPosition = new BlockPos(entityIn);
        return super.getPathToEntityLiving(entityIn);
    }

    @Override
    public boolean tryMoveToEntityLiving(Entity entityIn, double speedIn) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPathCalculationTime > pathRecalculationCooldown || lastPathCalculationTime == 0) {
            lastPathCalculationTime = currentTime;
            Path path = this.getPathToEntityLiving(entityIn);
            if (path != null) return this.setPath(path, speedIn);
            else {
                this.targetPosition = new BlockPos(entityIn);
                this.speed = speedIn;
                return true;
            }
        }
        return false;
    }

    @Override
    public void onUpdateNavigation() {
        if (!this.noPath()) super.onUpdateNavigation();
        else {
            if (this.targetPosition != null) {
                double distanceSq = this.creature.getDistanceSqToCenter(this.targetPosition);
                if (distanceSq > closeEnoughDistance * closeEnoughDistance) {
                    if (Math.sqrt(distanceSq) > minimumMovementThreshold) {
                        this.creature.getMoveHelper().setMoveTo(
                                (double)this.targetPosition.getX() + 0.5,
                                (double)this.targetPosition.getY(),
                                (double)this.targetPosition.getZ() + 0.5,
                                this.speed);
                    }
                } else {
                    // Entity is close enough, apply smoother stopping mechanism
                    this.creature.getNavigator().clearPath();
                    this.creature.getMoveHelper().setMoveTo(this.creature.posX, this.creature.posY, this.creature.posZ, 0);
                    this.targetPosition = null;
                }
            }
        }
    }
}
