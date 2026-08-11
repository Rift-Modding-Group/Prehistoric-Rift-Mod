package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureHitboxed;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.AxisAlignedBB;
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
        if (this.creature.getCreatureType().getNavigation().getCanSwim()) return false;
        if (!this.creature.bodyTouchingLiquid()) return false;
        this.landBlockPos = this.nearestLandBlock();
        return this.landBlockPos != null;
    }


    @Override
    public boolean shouldContinueExecuting() {
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
        this.creature.getMoveHelper().setMoveTo(this.landBlockPos.getX(), this.landBlockPos.getY(), this.landBlockPos.getZ(), 1D);
    }

    //look for a land block with sufficient space on top to go to
    @Nullable
    private BlockPos nearestLandBlock() {
        double bodyYPos = (this.creature instanceof RiftCreatureHitboxed creatureHitboxed) ?
                creatureHitboxed.getMultiHitboxList().getCollisionHitboxByName("body").posY : this.creature.posY;
        int horizontalDetectBound = (int) Math.ceil(DETECT_RANGE / 2);

        BlockPos closest = null;
        double closestDistanceSq = Double.MAX_VALUE;
        BlockPos.MutableBlockPos posToTest = new BlockPos.MutableBlockPos();
        for (int x = -horizontalDetectBound; x <= horizontalDetectBound; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -horizontalDetectBound; z <= horizontalDetectBound; z++) {
                    posToTest.setPos(this.creature.posX + x, bodyYPos + y, this.creature.posZ + z);
                    if (this.creature.world.getBlockState(posToTest).getMaterial() != Material.AIR) continue;

                    double distanceSq = posToTest.distanceSq(
                            this.creature.posX,
                            this.creature.posY,
                            this.creature.posZ
                    );
                    if (distanceSq >= closestDistanceSq || !this.canFitHitbox(posToTest)) continue;

                    closest = posToTest.toImmutable();
                    closestDistanceSq = distanceSq;
                }
            }
        }
        return closest;
    }

    private boolean canFitHitbox(@NotNull BlockPos pos) {
        if (!this.creature.world.getBlockState(pos.down()).getMaterial().isSolid()) return false;

        AxisAlignedBB creatureBounds = this.creature.getEntityBoundingBox();
        AxisAlignedBB shoreBounds = creatureBounds.offset(
                pos.getX() - this.creature.posX,
                pos.getY() - creatureBounds.minY,
                pos.getZ() - this.creature.posZ
        );
        return !this.creature.world.collidesWithAnyBlock(shoreBounds);
    }
}
