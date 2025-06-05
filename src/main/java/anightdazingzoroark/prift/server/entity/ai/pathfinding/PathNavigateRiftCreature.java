package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PathNavigateRiftCreature extends PathNavigateGround {
    private final RiftCreature creature;

    public PathNavigateRiftCreature(RiftCreature creature, World worldIn) {
        super(creature, worldIn);
        this.creature = creature;
    }

    @Override
    public Path getPathToEntityLiving(Entity entityIn) {
        return super.getPathToEntityLiving(entityIn);
    }

    @Override
    public Path getPathToPos(BlockPos pos) {
        if (this.world.getBlockState(pos).getMaterial() == Material.AIR) {
            BlockPos blockpos;

            for (blockpos = pos.down(); blockpos.getY() > 0 && this.world.getBlockState(blockpos).getMaterial() == Material.AIR; blockpos = blockpos.down()) {}

            if (blockpos.getY() > 0) {
                return super.getPathToPos(blockpos.up());
            }

            while (blockpos.getY() < this.world.getHeight() && this.world.getBlockState(blockpos).getMaterial() == Material.AIR) {
                blockpos = blockpos.up();
            }

            pos = blockpos;
        }

        if (!this.world.getBlockState(pos).getMaterial().isSolid()) {
            return super.getPathToPos(pos);
        }
        else {
            BlockPos blockpos1;

            for (blockpos1 = pos.up(); blockpos1.getY() < this.world.getHeight() && this.world.getBlockState(blockpos1).getMaterial().isSolid(); blockpos1 = blockpos1.up())
            {
                ;
            }

            return super.getPathToPos(blockpos1);
        }
    }
}
