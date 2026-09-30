package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RiftGoToLandFromWater extends EntityAIBase {
    private static final double DETECT_RANGE = 16;
    @NotNull
    private final RiftCreature creature;
    @Nullable
    protected BlockPos landBlockPos;

    public RiftGoToLandFromWater(@NotNull RiftCreature creature) {
        this.creature = creature;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        //cannot search when staggered
        if (this.creature.isStaggered()) return false;

        //cannot search when ridden
        if (this.creature.isBeingRidden()) return false;

        //cannot search if can swim, this is a landlubber only ai goal
        if (this.creature.getCreatureType().getNavigation().getCanSwim()) return false;

        //cannot search if not in water
        if (!this.creature.bodyTouchingLiquid()) return false;

        this.landBlockPos = this.creature.findNearestLandBlock(DETECT_RANGE, false);
        return this.landBlockPos != null;
    }


    @Override
    public boolean shouldContinueExecuting() {
        if (this.creature.isBeingRidden() || this.creature.isStaggered()) return false;
        BlockPos creaturePos = this.creature.getPosition();
        return this.creature.world.getBlockState(creaturePos).getMaterial() != Material.AIR
                || !this.creature.world.getBlockState(creaturePos.down()).getMaterial().isSolid();
    }

    @Override
    public void resetTask() {
        this.creature.getNavigator().clearPath();
        this.creature.getMoveHelper().setMoveTo(this.creature.posX, this.creature.posY, this.creature.posZ, 0D);
        this.landBlockPos = null;
    }

    @Override
    public void updateTask() {
        if (this.landBlockPos == null) return;

        double displacementX = this.landBlockPos.getX() - this.creature.posX;
        double displacementZ = this.landBlockPos.getZ() - this.creature.posZ;
        double shoreTransferDistanceSq = this.creature.width * this.creature.width;
        if (displacementX * displacementX + displacementZ * displacementZ <= shoreTransferDistanceSq) {
            this.creature.setPosition(
                    this.landBlockPos.getX(),
                    this.landBlockPos.getY(),
                    this.landBlockPos.getZ()
            );
            this.creature.getNavigator().clearPath();
            this.creature.getMoveHelper().setMoveTo(
                    this.creature.posX,
                    this.creature.posY,
                    this.creature.posZ,
                    0D
            );
            return;
        }
        this.creature.getMoveHelper().setMoveTo(this.landBlockPos.getX(), this.landBlockPos.getY(), this.landBlockPos.getZ(), 1D);
    }

}
