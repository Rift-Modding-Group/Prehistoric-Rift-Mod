package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.SwimNodeProcessor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class PathNavigateRiftSwimmer extends PathNavigateSwimmer {
    public PathNavigateRiftSwimmer(EntityLiving entitylivingIn, World worldIn) {
        super(entitylivingIn, worldIn);
    }

    @Override
    protected void pathFollow() {
        Vec3d vec3d = this.getEntityPosition();
        double widthExpanded = Math.pow(Math.ceil(this.entity.width), 2);

        if (vec3d.squareDistanceTo(this.currentPath.getVectorFromIndex(this.entity, this.currentPath.getCurrentPathIndex())) < widthExpanded) {
            this.currentPath.incrementPathIndex();
        }

        for (int j = Math.min(this.currentPath.getCurrentPathIndex() + 6, this.currentPath.getCurrentPathLength() - 1); j > this.currentPath.getCurrentPathIndex(); --j) {
            Vec3d vec3d1 = this.currentPath.getVectorFromIndex(this.entity, j);

            if (vec3d1.squareDistanceTo(vec3d) <= 36.0D && this.isDirectPathBetweenPoints(vec3d, vec3d1, 0, 0, 0)) {
                this.currentPath.setCurrentPathIndex(j);
                break;
            }
        }

        this.checkForStuck(vec3d);
    }

    @Override
    protected PathFinder getPathFinder() {
        //this modified swim node processor ensures that when an aquatic creature is in a pit
        //of water that is less than their height, they can not only navigate properly, but
        //they can also navigate while not trying to breach too much
        //it also prevents them from going too near to shore
        return new PathFinder(new SwimNodeProcessor() {
            //stops creature from pausing when in a 1 block pit
            @Override
            public void init(IBlockAccess sourceIn, EntityLiving mob) {
                this.blockaccess = sourceIn;
                this.entity = mob;
                this.pointMap.clearMap();
                this.entitySizeX = MathHelper.floor(mob.width + 1);
                this.entitySizeY = 1;
                this.entitySizeZ = MathHelper.floor(mob.width + 1);
            }

            @Override
            public int findPathOptions(PathPoint[] pathOptions, PathPoint currentPoint, PathPoint targetPoint, float maxDistance) {
                int i = 0;

                for (EnumFacing enumfacing : EnumFacing.values()) {
                    //skip expansion above if within 4 blocks upward theres no water
                    if (enumfacing == EnumFacing.UP) {
                        BlockPos abovePos = new BlockPos(currentPoint.x, currentPoint.y + 1, currentPoint.z);
                        IBlockState aboveState = this.blockaccess.getBlockState(abovePos);
                        // dont path upward if the next block isn’t water
                        if (aboveState.getMaterial() != Material.WATER) continue;
                    }

                    PathPoint pathpoint = this.getWaterNode(
                            currentPoint.x + enumfacing.getXOffset(),
                            currentPoint.y + enumfacing.getYOffset(),
                            currentPoint.z + enumfacing.getZOffset()
                    );

                    if (pathpoint != null && !pathpoint.visited && pathpoint.distanceTo(targetPoint) < maxDistance) {
                        pathOptions[i++] = pathpoint;
                    }
                }

                return i;
            }
        });
    }
}
