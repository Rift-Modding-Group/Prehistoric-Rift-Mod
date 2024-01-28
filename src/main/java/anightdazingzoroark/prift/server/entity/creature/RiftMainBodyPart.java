package anightdazingzoroark.prift.server.entity.creature;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class RiftMainBodyPart extends RiftCreaturePart {
    private double yFloatPos;

    public RiftMainBodyPart(EntityLiving parent, float radius, float angleYaw, float offsetY, float sizeX, float sizeY, float damageMultiplier) {
        super(parent, radius, angleYaw, offsetY, sizeX, sizeY, damageMultiplier);
        this.yFloatPos = 0f;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.world.isRemote) {
            RiftCreature creature = (RiftCreature)this.parent;
            if (!creature.isFloating && this.isInWater()) {
                this.yFloatPos = this.getHighestWaterLevel();
                if (this.posY <= this.yFloatPos) creature.isFloating = true;
            }
            else if (creature.isFloating && this.isInWater()) {
                if (this.posY < this.yFloatPos) {
                    creature.motionY = 0.1;
                    creature.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
                }
                else {
                    creature.motionY = 0;
                    creature.setPosition(this.posX, this.yFloatPos - this.offsetY, this.posZ);
                    creature.stepHeight = 3;
                    creature.fallDistance = 0;
                }
            }
            else if (creature.isFloating && !creature.isInWater()) {
                creature.isFloating = false;
            }
        }
    }

    public float getHighestWaterLevel() {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        int i = MathHelper.floor(axisalignedbb.minX);
        int j = MathHelper.ceil(axisalignedbb.maxX);
        int k = MathHelper.floor(axisalignedbb.minY);
        int l = 256;
        int i1 = MathHelper.floor(axisalignedbb.minZ);
        int j1 = MathHelper.ceil(axisalignedbb.maxZ);
        BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

        try {
            for (int k1 = k; k1 < l; ++k1) {
                for (int l1 = i; l1 < j; ++l1) {
                    for (int i2 = i1; i2 < j1; ++i2) {
                        blockpos$pooledmutableblockpos.setPos(l1, k1, i2);
                        IBlockState iblockstate = this.world.getBlockState(blockpos$pooledmutableblockpos);

                        if (iblockstate.getMaterial() == Material.WATER) {
                            // Check if the block above is not water
                            IBlockState iblockstateAbove = this.world.getBlockState(blockpos$pooledmutableblockpos.up());
                            if (iblockstateAbove.getMaterial() != Material.WATER) {
                                return (float) k1;
                            }
                        }
                    }
                }
            }
            return (float) l;
        }
        finally {
            blockpos$pooledmutableblockpos.release();
        }
    }
}
