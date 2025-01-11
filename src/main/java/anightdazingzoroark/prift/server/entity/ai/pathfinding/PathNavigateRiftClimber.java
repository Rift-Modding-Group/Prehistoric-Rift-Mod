package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class PathNavigateRiftClimber extends PathNavigateGround {
    private final RiftCreature creature;
    private BlockPos targetPosition;
    private boolean stopFlag = false;

    public PathNavigateRiftClimber(RiftCreature creature, World worldIn) {
        super(creature, worldIn);
        this.creature = creature;
    }

    public Path getPathToPos(BlockPos pos) {
        this.targetPosition = pos;
        return super.getPathToPos(pos);
    }

    public Path getPathToEntityLiving(Entity entityIn) {
        this.targetPosition = new BlockPos(entityIn);
        return super.getPathToEntityLiving(entityIn);
    }

    public boolean tryMoveToEntityLiving(Entity entityIn, double speedIn) {
        Path path = this.getPathToEntityLiving(entityIn);

        if (path != null) return this.setPath(path, speedIn);
        else {
            this.targetPosition = new BlockPos(entityIn);
            this.speed = speedIn;
            return true;
        }
    }
    public void clearPath() {
        super.clearPath();
        this.stopFlag = true;
    }

    public void onUpdateNavigation() {
        if (!this.noPath()) super.onUpdateNavigation();
        else if (this.targetPosition != null) {
            double d0 = this.creature.width * this.creature.width;
            if (this.creature.getDistanceSqToCenter(this.targetPosition) >= d0
                    && (this.creature.posY <= (double)this.targetPosition.getY()
                    || this.creature.getDistanceSqToCenter(new BlockPos(this.targetPosition.getX(), MathHelper.floor(this.creature.posY), this.targetPosition.getZ())) >= d0))
            {
                this.creature.getMoveHelper().setMoveTo(this.targetPosition.getX(), this.targetPosition.getY(), this.targetPosition.getZ(), this.speed);
            }
            else this.targetPosition = null;
        }

        if (this.stopFlag) {
            this.stopFlag = false;
            this.targetPosition = null;
        }
    }
}
