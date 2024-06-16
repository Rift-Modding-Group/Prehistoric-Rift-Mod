package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualExtruderRecipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileEntitySemiManualExtruder extends TileEntitySemiManualBase {
    private float rotation = 0f;

    @Override
    public void update() {
        super.update();
        if (!this.world.isRemote) {
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
                            boolean outputUsability = (this.getOutpuItem().isEmpty() || ((SemiManualExtruderRecipe)this.getTopTEntity().getCurrentRecipe()).output.apply(this.getOutpuItem())) && this.getOutpuItem().getCount() < this.getOutpuItem().getMaxStackSize();
                            if (outputUsability) {
                                if (this.getTopTEntity().getTimeHeld() < this.getTopTEntity().getMaxRecipeTime()) {
                                    this.getTopTEntity().setTimeHeld(this.getTopTEntity().getTimeHeld() + 1);
                                }
                                else {
                                    IItemHandler itemHandler = this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                                    if (itemHandler != null) {
                                        ItemStack outputStack = ((SemiManualExtruderRecipe)this.getTopTEntity().getCurrentRecipe()).output.getMatchingStacks()[0].copy();
                                        ItemHandlerHelper.insertItemStacked(itemHandler, outputStack, false);
                                    }
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
                        this.setRotation(this.rotation + (float) this.getTopTEntity().getPower());
                        if (this.getRotation() >= 360f) this.setRotation(this.rotation - 360f);
                    }
                }
            }
        }
    }

    public float getRotation() {
        return this.rotation;
    }

    public void setRotation(float value) {
        this.rotation = value;
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public ItemStack getOutpuItem() {
        IItemHandler itemHandler = this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (itemHandler != null) return itemHandler.getStackInSlot(1);
        return null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setFloat("rotation", this.rotation);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.rotation = compound.getInteger("rotation");
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        this.rotation = tag.getInteger("rotation");
    }
}