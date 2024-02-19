package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PathNavigateRiftWaterCreature extends PathNavigateSwimmer {
    private RiftWaterCreature creature;

    public PathNavigateRiftWaterCreature(RiftWaterCreature entityIn, World worldIn) {
        super(entityIn, worldIn);
        this.creature = entityIn;
    }

    @Override
    public void onUpdateNavigation() {
        ++this.totalTicks;
        if (this.tryUpdatePath) this.updatePath();

        if (!this.noPath()) {
            if (this.canNavigate()) this.pathFollow();
            else if (this.currentPath != null && this.currentPath.getCurrentPathIndex() < this.currentPath.getCurrentPathLength()) {
                Vec3d pos = this.currentPath.getVectorFromIndex(this.creature, this.currentPath.getCurrentPathIndex());
                // Ensure target position is below water surface
                int waterSurfaceY = this.findWaterSurfaceY(new BlockPos(pos.x, pos.y, pos.z));
                pos = new Vec3d(pos.x, Math.min(pos.y, waterSurfaceY), pos.z);

                if (MathHelper.floor(this.creature.posX) == MathHelper.floor(pos.x) && MathHelper.floor(this.creature.posY) == MathHelper.floor(pos.y) && MathHelper.floor(this.creature.posZ) == MathHelper.floor(pos.z))
                    this.currentPath.incrementPathIndex();
            }

            this.debugPathFinding();
            if (!this.noPath() && this.currentPath != null) {
                Vec3d pos = this.currentPath.getPosition(this.creature);
                int waterSurfaceY = this.findWaterSurfaceY(new BlockPos(pos.x, pos.y, pos.z));
                pos = new Vec3d(pos.x, Math.min(pos.y, waterSurfaceY), pos.z);

                this.creature.getMoveHelper().setMoveTo(pos.x, pos.y, pos.z, this.speed);
            }
        }
    }

    private int findWaterSurfaceY(BlockPos pos) {
        for (int y = pos.getY(); y < this.world.getHeight(); y++) {
            BlockPos upPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (!this.world.getBlockState(upPos).getMaterial().isLiquid()) {
                return y - 2;
            }
        }
        return this.world.getSeaLevel();
    }

    @Override
    protected void pathFollow() {
        if (this.currentPath != null) {
            double entityWidth = this.creature.width > 0.75 ? this.creature.width / 2 : 0.75 - this.creature.width / 2;
            if (Math.abs(this.creature.motionX) > 0.2 || Math.abs(this.creature.motionZ) > 0.2)
                entityWidth *= Math.sqrt(this.creature.motionX * this.creature.motionX + this.creature.motionY * this.creature.motionY + this.creature.motionZ * this.creature.motionZ) * 6;

            Vec3d pos = this.currentPath.getCurrentPos();
            if (Math.abs(this.creature.posX - (pos.x + 0.5)) < entityWidth && Math.abs(this.creature.posZ - (pos.z + 0.5)) < entityWidth && Math.abs(this.creature.posY - pos.y) < entityWidth * 2)
                this.currentPath.incrementPathIndex();

            final Vec3d entityPos = this.getEntityPosition();
            for (int j = Math.min(this.currentPath.getCurrentPathIndex() + 6, this.currentPath.getCurrentPathLength() - 1); j > this.currentPath.getCurrentPathIndex(); --j) {
                pos = this.currentPath.getVectorFromIndex(this.creature, j);

                if(!(pos.squareDistanceTo(entityPos) > 36) && this.isDirectPathBetweenPoints(entityPos, pos, 0, 0, 0)) {
                    this.currentPath.setCurrentPathIndex(j);
                    break;
                }
            }

            this.checkForStuck(entityPos);
        }
    }
}
