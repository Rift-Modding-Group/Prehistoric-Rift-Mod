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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class PathNavigateRiftSwimmer extends PathNavigateSwimmer {
    public PathNavigateRiftSwimmer(EntityLiving entitylivingIn, World worldIn) {
        super(entitylivingIn, worldIn);
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
                this.entitySizeY = MathHelper.floor(mob.height);
                this.entitySizeZ = MathHelper.floor(mob.width + 1);
            }

            @Override
            public int findPathOptions(PathPoint[] pathOptions, PathPoint currentPoint, PathPoint targetPoint, float maxDistance) {
                int i = 0;

                mainLoop: for (EnumFacing enumfacing : EnumFacing.values()) {
                    //skip expansion above if within 4 blocks upward theres no water
                    if (enumfacing == EnumFacing.UP) {
                        BlockPos abovePos = new BlockPos(currentPoint.x, currentPoint.y + 1, currentPoint.z);
                        IBlockState aboveState = this.blockaccess.getBlockState(abovePos);
                        // dont path upward if the next block isn’t water
                        if (aboveState.getMaterial() != Material.WATER) continue;
                    }
                    //skip expansion in a cardinal direction if within 4 blocks theres no water
                    else if (enumfacing == EnumFacing.NORTH || enumfacing == EnumFacing.SOUTH || enumfacing == EnumFacing.EAST || enumfacing == EnumFacing.WEST) {
                        for (int j = 1; j <= 4; j++) {
                            BlockPos displacedPos = null;

                            switch (enumfacing) {
                                case NORTH:
                                    displacedPos = new BlockPos(currentPoint.x, currentPoint.y, currentPoint.z - j);
                                    break;
                                case SOUTH:
                                    displacedPos = new BlockPos(currentPoint.x, currentPoint.y, currentPoint.z + j);
                                    break;
                                case EAST:
                                    displacedPos = new BlockPos(currentPoint.x + j, currentPoint.y, currentPoint.z);
                                    break;
                                case WEST:
                                    displacedPos = new BlockPos(currentPoint.x - j, currentPoint.y, currentPoint.z);
                                    break;
                            }

                            if (displacedPos != null) {
                                IBlockState stateAtPos = this.blockaccess.getBlockState(displacedPos);
                                //dont path if theres no water in that direction
                                if (stateAtPos.getMaterial() != Material.WATER) continue mainLoop;
                            }
                        }
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
