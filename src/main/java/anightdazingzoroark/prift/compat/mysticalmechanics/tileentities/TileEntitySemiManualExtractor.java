package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualExtractorRecipe;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class TileEntitySemiManualExtractor extends TileEntitySemiManualBase implements IFluidHandler {
    private final FluidTank tank;

    public TileEntitySemiManualExtractor() {
        super(3);
        this.tank = new FluidTank(4000) {
            @Override
            protected void onContentsChanged() {
                markDirty();
                if (!world.isRemote) getWorld().notifyBlockUpdate(pos, getWorld().getBlockState(pos), getWorld().getBlockState(pos), 2);
            }
        };
        this.tank.setTileEntity(this);
        this.tank.setCanFill(false);
        this.tank.setCanDrain(true);
    }

    @Override
    public void update() {
        super.update();
        if (!this.world.isRemote) {
            //for creating fluid
            if (this.getTopTEntity() != null) {
                if (this.getTopTEntity().getPower() > 0) {
                    if (this.getTopTEntity().getCurrentRecipe() == null) {
                        for (SemiManualExtractorRecipe recipe : RiftMMRecipes.smExtractorRecipes) {
                            if (recipe.matches(this.getTopTEntity().getPower(), this.getInputItem())) {
                                this.getTopTEntity().setCurrentRecipe(recipe);
                            }
                        }
                    }
                    else {
                        if (!this.getTopTEntity().getMustBeReset() && !this.canDoResetAnim()) {
                            boolean tankUsability = this.tank.getFluid() == null || (this.tank.getFluid().getFluid() == ((SemiManualExtractorRecipe)this.getTopTEntity().getCurrentRecipe()).output.getFluid() && this.tank.getFluid().amount < 4000);
                            if (tankUsability) {
                                if (this.getTopTEntity().getTimeHeld() < this.getTopTEntity().getMaxRecipeTime()) {
                                    this.getTopTEntity().setTimeHeld(this.getTopTEntity().getTimeHeld() + 1);
                                }
                                else {
                                    this.tank.fillInternal(((SemiManualExtractorRecipe)this.getTopTEntity().getCurrentRecipe()).output, true);
                                    this.getInputItem().shrink(1);
                                    this.getTopTEntity().setTimeHeld(0);
                                    this.getTopTEntity().setMustBeReset(true);
                                }
                            }
                            if (!this.getTopTEntity().getCurrentRecipe().matches(this.getTopTEntity().getPower(), this.getInputItem())) {
                                this.getTopTEntity().setTimeHeld(0);
                                this.getTopTEntity().setCurrentRecipe(null);
                                this.getTopTEntity().setMustBeReset(true);
                            }
                        }
                    }
                }
            }

            //for bucket filling
            if (!this.getStackInSlot(1).isEmpty() && this.tank.getFluid() != null) {
                ItemStack toBeFilled = this.getStackInSlot(1).copy();
                toBeFilled.setCount(1);
                ItemStack filledBucket = RiftUtil.fillBucketWithFluid(toBeFilled, this.tank.getFluid().getFluid());
                if (this.getStackInSlot(2).isEmpty() && this.tank.getFluid().amount >= 1000) {
                    this.insertItemToSlot(2, filledBucket);
                    this.getStackInSlot(1).setCount(this.getStackInSlot(1).getCount() - 1);
                    this.tank.drain(1000, true);
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.tank.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        this.tank.writeToNBT(compound);
        return compound;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    public void onLoad() {
        if (!this.world.isRemote) {
            this.world.notifyBlockUpdate(this.pos, getWorld().getBlockState(this.pos), getWorld().getBlockState(this.pos), 2);
        }
        else {
            this.markDirty();
            this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
        }
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.tank);
        return super.getCapability(capability, facing);
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        this.tank.readFromNBT(tag);
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return this.tank.getTankProperties();
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return this.tank.fill(resource, doFill);
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return this.tank.drain(resource, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return this.tank.drain(maxDrain, doDrain);
    }

    public FluidTank getTank() {
        return this.tank;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        if (side.getAxis().isHorizontal()) {
            switch (this.getFacing()) {
                case NORTH:
                    if (side == EnumFacing.WEST) return new int[]{1};
                    else return new int[]{0};
                case SOUTH:
                    if (side == EnumFacing.EAST) return new int[]{1};
                    else return new int[]{0};
                case EAST:
                    if (side == EnumFacing.NORTH) return new int[]{1};
                    else return new int[]{0};
                case WEST:
                    if (side == EnumFacing.SOUTH) return new int[]{1};
                    else return new int[]{0};
            }
        }
        else if (side == EnumFacing.DOWN) return new int[]{2};
        return new int[]{0};
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        if (index == 1) return itemStackIn.getItem() == Items.BUCKET;
        return this.isItemValidForSlot(index, itemStackIn);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        if (index == 2) return direction == EnumFacing.DOWN;
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index != 2;
    }
}
