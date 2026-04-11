package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualExtractorRecipe;
import anightdazingzoroark.prift.server.entity.inventory.RiftInventoryHandler;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TileEntitySemiManualExtractor extends TileEntitySemiManualBase implements IGuiHolder<PosGuiData> {
    private final int maxVolume = 4000;

    @Override
    public void registerInventories() {
        super.registerInventories();
        this.registerInventory("BucketInput", new RiftInventoryHandler(1, itemStack -> {
            return itemStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        }));
        this.registerInventory("BucketOutput", 1);
        this.registerInventorySiding("BucketOutput", SideInvInteraction.EXTRACT, EnumFacing.DOWN);
    }

    @Override
    public void registerFluidTanks() {
        this.registerFluidTank("OutputTank", this.maxVolume);
    }

    @Override
    public void update() {
        super.update();
        if (this.world.isRemote) return;

        FluidTank outputTank = this.getOutputTank();

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
                        SemiManualExtractorRecipe currentRecipe = (SemiManualExtractorRecipe) this.getTopTEntity().getCurrentRecipe();
                        boolean tankUsability = outputTank.getFluid() == null || (outputTank.getFluid().getFluid() == currentRecipe.output.getFluid() && outputTank.getFluid().amount < this.maxVolume);
                        if (tankUsability) {
                            if (this.getTopTEntity().getTimeHeld() < this.getTopTEntity().getMaxRecipeTime()) {
                                this.getTopTEntity().setTimeHeld(this.getTopTEntity().getTimeHeld() + 1);
                            }
                            else {
                                outputTank.fillInternal(currentRecipe.output, true);
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
        RiftInventoryHandler bucketInput = this.getBucketInput();
        RiftInventoryHandler bucketOutput = this.getBucketOutput();
        if (!bucketInput.isEmpty() && outputTank.getFluid() != null) {
            ItemStack bucket = bucketInput.getStackInSlot(1);
            ItemStack filledBucket = RiftUtil.fillBucketWithFluid(bucket.copy(), outputTank.getFluid().getFluid());
            if (bucketOutput.isEmpty() && outputTank.getFluid().amount >= Fluid.BUCKET_VOLUME) {
                bucketOutput.insertItem(filledBucket);
                bucket.setCount(bucket.getCount() - 1);
                outputTank.drain(1000, true);
            }
        }
    }

    public FluidTank getOutputTank() {
        return this.getFluidTank("OutputTank");
    }

    public RiftInventoryHandler getBucketInput() {
        return this.getInventory("BucketInput");
    }

    public RiftInventoryHandler getBucketOutput() {
        return this.getInventory("BucketOutput");
    }

    @Override
    public ModularPanel buildUI(PosGuiData posGuiData, PanelSyncManager panelSyncManager, UISettings uiSettings) {
        return null;
    }
}
