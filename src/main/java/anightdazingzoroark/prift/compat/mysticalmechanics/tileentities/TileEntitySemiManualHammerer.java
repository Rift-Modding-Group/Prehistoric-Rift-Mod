package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualExtruderRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.basic.recipe.AnvilRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.ModuleTechBloomery;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.block.BlockBloom;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.BloomAnvilRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.WitherForgeRecipe;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileEntitySemiManualHammerer extends TileEntitySemiManualBase {
    public TileEntitySemiManualHammerer() {
        super(4);
    }

    @Override
    public void update() {
        super.update();
        if (!this.world.isRemote) {
            if (this.getTopTEntity() != null) {
                TileEntitySemiManualHammererTop hammerer = (TileEntitySemiManualHammererTop)this.getTopTEntity();
                if (hammerer.getPower() > 0) {
                    if (hammerer.getHammererRecipe() == null) {
                        boolean canGetRecipe = this.getInputItem().getItem() instanceof ItemBlock && ((ItemBlock) this.getInputItem().getItem()).getBlock() instanceof BlockBloom;
                        if (canGetRecipe) {
                            NBTTagCompound nbtTagCompound = this.getInputItem().getTagCompound();
                            NBTTagCompound bloomTagCompound = nbtTagCompound != null ? nbtTagCompound.getCompoundTag("BlockEntityTag") : null;
                            if (bloomTagCompound != null && bloomTagCompound.hasKey("recipeId") && hammerer.getPower() >= 20) {
                                if (ModuleTechBloomery.Registries.BLOOMERY_RECIPE.getValue(new ResourceLocation(bloomTagCompound.getString("recipeId"))) != null) {
                                    hammerer.setHammererRecipe(ModuleTechBloomery.Registries.BLOOMERY_RECIPE.getValue(new ResourceLocation(bloomTagCompound.getString("recipeId"))));
                                }
                                else if (ModuleTechBloomery.Registries.WITHER_FORGE_RECIPE.getValue(new ResourceLocation(bloomTagCompound.getString("recipeId"))) != null) {
                                    hammerer.setHammererRecipe(ModuleTechBloomery.Registries.WITHER_FORGE_RECIPE.getValue(new ResourceLocation(bloomTagCompound.getString("recipeId"))));
                                }
                            }
                        }
                    }
                    else {
                        if (!hammerer.getMustBeReset() && !this.canDoResetAnim()) {
                            boolean outputFull = this.getStackInSlot(1).getCount() + 12 < this.getStackInSlot(1).getMaxStackSize() && this.getStackInSlot(2).getCount() + 2 < this.getStackInSlot(2).getMaxStackSize() && this.getStackInSlot(3).getCount() + 2 < this.getStackInSlot(3).getMaxStackSize();
                            boolean outputOneUsed = this.getStackInSlot(1).isEmpty() || Ingredient.fromStacks(BloomAnvilRecipe.getRecipe(this.getInputItem(), AnvilRecipe.EnumTier.OBSIDIAN, AnvilRecipe.EnumType.HAMMER).getOutput()).apply(this.getStackInSlot(1));
                            boolean outputTwoUsed = this.getStackInSlot(2).isEmpty() || Ingredient.fromStacks(hammerer.getHammererRecipe().getSlagItemStack()).apply(this.getStackInSlot(2));
                            boolean outputThreeUsed = this.getStackInSlot(3).isEmpty() || Ingredient.fromStacks(hammerer.getHammererRecipe().getFailureItems()[0].getItemStack()).apply(this.getStackInSlot(3));

                            boolean outputUsability = BloomAnvilRecipe.getRecipe(this.getInputItem(), AnvilRecipe.EnumTier.OBSIDIAN, AnvilRecipe.EnumType.HAMMER) != null && outputOneUsed && outputTwoUsed && outputThreeUsed && outputFull && hammerer.getMinPower() != -1D;
                            if (outputUsability) {
                                if (hammerer.getTimeHeld() < hammerer.getMaxHammererTime()) {
                                    hammerer.setTimeHeld(hammerer.getTimeHeld() + 1);
                                }
                                else {
                                    ItemStack outputStack = hammerer.getHammererRecipe().getOutput().copy();
                                    outputStack.setCount(12);
                                    this.insertItemToSlot(1, outputStack);

                                    ItemStack outputSlagStack = hammerer.getHammererRecipe().getSlagItemStack().copy();
                                    outputSlagStack.setCount(2);
                                    this.insertItemToSlot(2, outputSlagStack);

                                    ItemStack failItem = hammerer.getHammererRecipe().getFailureItems()[0].getItemStack();
                                    failItem.setCount(2);
                                    this.insertItemToSlot(3, failItem);

                                    this.getInputItem().shrink(1);
                                    hammerer.setTimeHeld(0);
                                    hammerer.setMustBeReset(true);
                                }
                            }
                            if (!Ingredient.fromStacks(hammerer.getHammererRecipe().getOutputBloom()).apply(this.getInputItem()) || hammerer.getPower() < hammerer.getMinPower()) {
                                hammerer.setTimeHeld(0);
                                hammerer.setHammererRecipe(null);
                                hammerer.setMustBeReset(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        if (side == EnumFacing.DOWN) return new int[]{1, 2, 3};
        return new int[]{0};
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        if (index != 0) return direction == EnumFacing.DOWN;
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index == 0;
    }
}
