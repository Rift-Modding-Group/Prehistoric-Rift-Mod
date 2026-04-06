package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualExtruderRecipe;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualPresserRecipe;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.DoublePropertyValue;
import anightdazingzoroark.prift.server.entity.inventory.RiftInventoryHandler;
import anightdazingzoroark.riftlib.core.AnimatableValue;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Arrays;
import java.util.List;

public class TileEntitySemiManualExtruder extends TileEntitySemiManualBase {
    @Override
    public void registerValues() {
        super.registerValues();
        this.registerValue(new DoublePropertyValue("Rotation", 0D));
    }

    @Override
    public void registerInventories() {
        super.registerInventories();
        this.registerInventory("Output", 1);
        this.registerInventorySiding("Output", SideInvInteraction.EXTRACT, EnumFacing.DOWN);
    }

    @Override
    public void update() {
        super.update();
        if (this.world.isRemote) return;
        if (this.getTopTEntity() != null) {
            if (this.getTopTEntity().getPower() > 0) {
                if (this.getTopTEntity().getCurrentRecipe() == null) {
                    for (SemiManualExtruderRecipe recipe : RiftMMRecipes.smExtruderRecipes) {
                        if (recipe.matches(this.getTopTEntity().getPower(), this.getInputItem())) {
                            this.getTopTEntity().setCurrentRecipe(recipe);
                        }
                    }
                }
                else {
                    if (!this.getTopTEntity().getMustBeReset() && !this.canDoResetAnim()) {
                        SemiManualExtruderRecipe currentRecipe = (SemiManualExtruderRecipe) this.getTopTEntity().getCurrentRecipe();
                        boolean outputUsability = (this.getOutpuItem().isEmpty() || currentRecipe.output.apply(this.getOutpuItem())) && this.getOutpuItem().getCount() + currentRecipe.output.matchingStacks[0].getCount() <= this.getOutpuItem().getMaxStackSize();
                        if (outputUsability) {
                            if (this.getTopTEntity().getTimeHeld() < this.getTopTEntity().getMaxRecipeTime()) {
                                this.getTopTEntity().setTimeHeld(this.getTopTEntity().getTimeHeld() + 1);
                            }
                            else {
                                RiftInventoryHandler outputInv = this.getOutputInventory();
                                ItemStack outputStack = currentRecipe.output.getMatchingStacks()[0].copy();
                                outputInv.insertItem(outputStack);
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

                //rotation update
                if (!this.getTopTEntity().getMustBeReset()) {
                    double newRotation = this.getRotation() + this.getTopTEntity().getPower();
                    if (newRotation >= 360f) newRotation = newRotation - 360f;
                    this.setRotation(newRotation);
                }
            }
        }
    }

    public RiftInventoryHandler getOutputInventory() {
        return this.getInventory("Output");
    }

    public double getRotation() {
        return this.getValue("Rotation");
    }

    public void setRotation(double value) {
        this.setValue("Rotation", value);
    }

    public ItemStack getOutpuItem() {
        return this.getStackInSlot(1);
    }

    @Override
    public List<AnimatableValue> createAnimationVariables() {
        return List.of(new AnimatableValue("spinAxle", 0D));
    }

    @Override
    public List<AnimatableValue> tickAnimationVariables() {
        return List.of(new AnimatableValue("spinAxle", this.getRotation()));
    }
}
