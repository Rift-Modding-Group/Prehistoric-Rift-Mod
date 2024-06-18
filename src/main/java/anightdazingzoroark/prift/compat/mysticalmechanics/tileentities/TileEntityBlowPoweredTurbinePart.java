package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class TileEntityBlowPoweredTurbinePart extends TileEntity {
    private BlockPos centerBlockPos;

    public BlockPos getCenterBlockPos() {
        return this.centerBlockPos;
    }

    public void setCenterBlockPos(BlockPos centerBlockPos) {
        this.centerBlockPos = centerBlockPos;
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setFloat("centerPosX", this.centerBlockPos.getX());
        compound.setFloat("centerPosY", this.centerBlockPos.getY());
        compound.setFloat("centerPosZ", this.centerBlockPos.getZ());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.centerBlockPos = new BlockPos(compound.getInteger("centerPosX"), compound.getInteger("centerPosY"), compound.getInteger("centerPosZ"));
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        this.centerBlockPos = new BlockPos(tag.getInteger("centerPosX"), tag.getInteger("centerPosY"), tag.getInteger("centerPosZ"));
    }
}
