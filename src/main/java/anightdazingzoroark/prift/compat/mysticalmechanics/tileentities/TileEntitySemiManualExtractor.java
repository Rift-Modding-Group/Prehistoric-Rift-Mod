package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualExtractorRecipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
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
        System.out.println("has fluid: "+(this.tank.getFluid() != null));
        //for updating in case of desync
        FluidStack fluidStack;
        if (!this.world.isRemote) {
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
}
