package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualExtractorRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;

public class TileEntitySemiManualExtractor extends TileEntitySemiManualBase implements IFluidHandler {
    private final FluidTank tank;

    public TileEntitySemiManualExtractor() {
        super();
        this.tank = new FluidTank(4000) {
            @Override
            protected void onContentsChanged() {
                markDirty();
                getWorld().notifyBlockUpdate(pos, getWorld().getBlockState(pos), getWorld().getBlockState(pos), 2);
            }
        };
        this.tank.setTileEntity(this);
        this.tank.setCanFill(false);
        this.tank.setCanDrain(true);
    }

    @Override
    public void update() {
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
                    if (!this.getTopTEntity().getMustBeReset()) {
                        boolean tankUsability = this.tank.getFluid() == null || (this.tank.getFluid().getFluid() == this.getTopTEntity().getCurrentRecipe().output.getFluid() && this.tank.getFluid().amount < 4000);
                        if (tankUsability) {
                            if (this.getTopTEntity().getTimeHeld() < this.getTopTEntity().getMaxRecipeTime()) {
                                this.getTopTEntity().setTimeHeld(this.getTopTEntity().getTimeHeld() + 1);
                            }
                            else {
                                this.tank.fillInternal(this.getTopTEntity().getCurrentRecipe().output, true);
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
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.tank.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        this.tank.writeToNBT(compound);
        return compound;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return true;
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.tank);
        return super.getCapability(capability, facing);
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
    public void registerControllers(AnimationData animationData) {

    }
}
