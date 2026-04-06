package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.server.entity.inventory.RiftInventoryHandler;
import com.codetaylor.mc.pyrotech.modules.tech.basic.recipe.AnvilRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.ModuleTechBloomery;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.block.BlockBloom;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.BloomAnvilRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.BloomeryRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.BloomeryRecipeBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class TileEntitySemiManualHammerer extends TileEntitySemiManualBase {
    @Override
    public void registerInventories() {
        this.registerInventory("Input", new RiftInventoryHandler(1, itemStack -> {
            return itemStack.getItem() instanceof ItemBlock && ((ItemBlock) itemStack.getItem()).getBlock() instanceof BlockBloom;
        }));
        this.registerInventorySiding("Input", SideInvInteraction.INSERT, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST);

        this.registerInventory("Output", 1);
        this.registerInventorySiding("Output", SideInvInteraction.EXTRACT, EnumFacing.DOWN);
        this.registerInventory("SlagOutput", 1);
        this.registerInventorySiding("SlagOutput", SideInvInteraction.EXTRACT, EnumFacing.DOWN);
        this.registerInventory("FailOutput", 1);
        this.registerInventorySiding("FailOutput", SideInvInteraction.EXTRACT, EnumFacing.DOWN);
    }

    @Override
    public void update() {
        super.update();
        if (this.world.isRemote) return;
        if (this.getTopTEntity() != null) {
            TileEntitySemiManualHammererTop hammerer = (TileEntitySemiManualHammererTop) this.getTopTEntity();
            if (hammerer.getPower() > 0) {
                BloomeryRecipeBase<?> currentRecipe = hammerer.getHammererRecipe();
                if (currentRecipe == null) {
                    boolean canGetRecipe = this.getInputItem().getItem() instanceof ItemBlock && ((ItemBlock) this.getInputItem().getItem()).getBlock() instanceof BlockBloom;
                    if (canGetRecipe) {
                        NBTTagCompound nbtTagCompound = this.getInputItem().getTagCompound();
                        NBTTagCompound bloomTagCompound = nbtTagCompound != null ? nbtTagCompound.getCompoundTag("BlockEntityTag") : null;
                        if (bloomTagCompound != null && bloomTagCompound.hasKey("recipeId") && hammerer.getPower() >= 20) {
                            if (ModuleTechBloomery.Registries.BLOOMERY_RECIPE.getValue(new ResourceLocation(bloomTagCompound.getString("recipeId"))) != null) {
                                hammerer.setCurrentRecipeId(bloomTagCompound.getString("recipeId"));
                            }
                            else if (ModuleTechBloomery.Registries.WITHER_FORGE_RECIPE.getValue(new ResourceLocation(bloomTagCompound.getString("recipeId"))) != null) {
                                hammerer.setCurrentRecipeId(bloomTagCompound.getString("recipeId"));
                            }
                        }
                    }
                }
                else {
                    if (!hammerer.getMustBeReset() && !this.canDoResetAnim()) {
                        int mainAmnt = currentRecipe instanceof BloomeryRecipe ? 12 : 36;
                        int exAmnt = currentRecipe instanceof BloomeryRecipe ? 2 : 6;

                        ItemStack outputItem = this.getOutputInventory().getStackInSlot(0);
                        boolean outputInvFull = outputItem.getCount() + mainAmnt <= outputItem.getMaxStackSize();
                        ItemStack slagItem = this.getSlagInventory().getStackInSlot(0);
                        boolean slagInvFull = slagItem.getCount() + mainAmnt <= slagItem.getMaxStackSize();
                        ItemStack failItem = this.getFailOutputInventory().getStackInSlot(0);
                        boolean failInvFull = failItem.getCount() + mainAmnt <= failItem.getMaxStackSize();

                        boolean outputFull = outputInvFull && slagInvFull && failInvFull;
                        boolean outputOneUsed = outputItem.isEmpty() || Ingredient.fromStacks(BloomAnvilRecipe.getRecipe(this.getInputItem(), AnvilRecipe.EnumTier.OBSIDIAN, AnvilRecipe.EnumType.HAMMER).getOutput()).apply(outputItem);
                        boolean outputTwoUsed = slagItem.isEmpty() || Ingredient.fromStacks(currentRecipe.getSlagItemStack()).apply(slagItem);
                        boolean outputThreeUsed = failItem.isEmpty() || Ingredient.fromStacks(currentRecipe.getFailureItems()[0].getItemStack()).apply(failItem);

                        boolean outputUsability = BloomAnvilRecipe.getRecipe(this.getInputItem(), AnvilRecipe.EnumTier.OBSIDIAN, AnvilRecipe.EnumType.HAMMER) != null
                                && outputOneUsed && outputTwoUsed && outputThreeUsed && outputFull && hammerer.getMinPower() != -1D;
                        if (outputUsability) {
                            int timeHeld = hammerer.getTimeHeld();
                            if (timeHeld < hammerer.getMaxHammererTime()) {
                                hammerer.setTimeHeld(timeHeld + 1);
                            }
                            else {
                                ItemStack outputStack = currentRecipe.getOutput().copy();
                                outputStack.setCount(mainAmnt);
                                this.getOutputInventory().insertItem(outputStack);

                                ItemStack outputSlagStack = currentRecipe.getSlagItemStack().copy();
                                outputSlagStack.setCount(exAmnt);
                                this.getSlagInventory().insertItem(outputSlagStack);

                                ItemStack outputFailStack = currentRecipe.getFailureItems()[0].getItemStack();
                                outputFailStack.setCount(exAmnt);
                                this.getFailOutputInventory().insertItem(failItem);

                                this.getInputItem().shrink(1);
                                hammerer.setTimeHeld(0);
                                hammerer.setMustBeReset(true);
                            }
                        }
                        if (!Ingredient.fromStacks(currentRecipe.getOutputBloom()).apply(this.getInputItem()) || hammerer.getPower() < hammerer.getMinPower()) {
                            hammerer.setTimeHeld(0);
                            hammerer.setCurrentRecipeId("");
                            hammerer.setMustBeReset(true);
                        }
                    }
                }
            }
        }
    }

    private RiftInventoryHandler getOutputInventory() {
        return this.getInventory("Output");
    }

    private RiftInventoryHandler getSlagInventory() {
        return this.getInventory("SlagOutput");
    }

    private RiftInventoryHandler getFailOutputInventory() {
        return this.getInventory("FailOutput");
    }
}
