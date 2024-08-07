package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualPresserRecipe;
import net.minecraft.item.ItemStack;

public class TileEntitySemiManualPresser extends TileEntitySemiManualBase {
    public TileEntitySemiManualPresser() {
        super(2);
    }

    @Override
    public void update() {
        super.update();
        if (!this.world.isRemote) {
            if (this.getTopTEntity() != null) {
                if (this.getTopTEntity().getPower() > 0) {
                    if (this.getTopTEntity().getCurrentRecipe() == null) {
                        for (SemiManualPresserRecipe recipe : RiftMMRecipes.smPresserRecipes) {
                            if (recipe.matches(this.getTopTEntity().getPower(), this.getInputItem())) {
                                this.getTopTEntity().setCurrentRecipe(recipe);
                            }
                        }
                    }
                    else {
                        if (!this.getTopTEntity().getMustBeReset() && !this.canDoResetAnim()) {
                            boolean outputUsability = (this.getOutpuItem().isEmpty() || ((SemiManualPresserRecipe)this.getTopTEntity().getCurrentRecipe()).output.apply(this.getOutpuItem())) && this.getOutpuItem().getCount() + ((SemiManualPresserRecipe)this.getTopTEntity().getCurrentRecipe()).output.matchingStacks[0].getCount() <= this.getOutpuItem().getMaxStackSize();
                            if (outputUsability) {
                                if (this.getTopTEntity().getTimeHeld() < this.getTopTEntity().getMaxRecipeTime()) {
                                    this.getTopTEntity().setTimeHeld(this.getTopTEntity().getTimeHeld() + 1);
                                }
                                else {
                                    ItemStack outputStack = ((SemiManualPresserRecipe)this.getTopTEntity().getCurrentRecipe()).output.getMatchingStacks()[0].copy();
                                    this.insertItemToSlot(1, outputStack);
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

    public ItemStack getOutpuItem() {
        return this.getStackInSlot(1);
    }
}
