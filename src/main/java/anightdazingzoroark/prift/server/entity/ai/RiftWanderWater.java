package anightdazingzoroark.prift.server.entity.ai;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class RiftWanderWater extends RiftWander {
    public RiftWanderWater(EntityCreature creatureIn, double speedIn) {
        super(creatureIn, speedIn, 1);
    }

    @Override
    protected Vec3d getPosition() {
        Vec3d pos = RandomPositionGenerator.findRandomTarget(this.entity, 10, 7);
        if (pos != null) {
            BlockPos blockPos = new BlockPos(pos);
            for (int i = 0; !this.isWaterDestination(blockPos) && i < 10; i++) pos = RandomPositionGenerator.findRandomTarget(this.entity, 10, 7);
        }
        return pos;
    }

    @Override
    public void resetTask() {
        this.entity.getNavigator().clearPath();
    }

    private boolean isWaterDestination(BlockPos pos) {
        return this.entity.world.getBlockState(pos).getMaterial() == Material.WATER;
    }
}
