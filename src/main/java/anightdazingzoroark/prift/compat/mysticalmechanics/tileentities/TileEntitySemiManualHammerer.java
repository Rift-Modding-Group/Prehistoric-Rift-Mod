package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualExtruderRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.basic.recipe.AnvilRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.ModuleTechBloomery;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.block.BlockBloom;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.BloomAnvilRecipe;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileEntitySemiManualHammerer extends TileEntitySemiManualBase {
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
                                hammerer.setHammererRecipe(ModuleTechBloomery.Registries.BLOOMERY_RECIPE.getValue(new ResourceLocation(bloomTagCompound.getString("recipeId"))));
                            }
                        }
                    }
                    else {
                        System.out.println("max time: "+hammerer.getMaxHammererTime());
                        System.out.println("current time: "+hammerer.getTimeHeld());
                        if (!hammerer.getMustBeReset() && !this.canDoResetAnim()) {
                            boolean outputUsability = (this.getOutpuItem().isEmpty() || Ingredient.fromStacks(BloomAnvilRecipe.getRecipe(this.getInputItem(), AnvilRecipe.EnumTier.OBSIDIAN, AnvilRecipe.EnumType.HAMMER).getOutput()).apply(this.getOutpuItem())) && this.getOutpuItem().getCount() < this.getOutpuItem().getMaxStackSize();
                            if (outputUsability) {
                                if (hammerer.getTimeHeld() < hammerer.getMaxHammererTime()) {
                                    hammerer.setTimeHeld(hammerer.getTimeHeld() + 1);
                                }
                                else {
                                    IItemHandler itemHandler = this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                                    if (itemHandler != null) {
                                        ItemStack outputStack = BloomAnvilRecipe.getRecipe(this.getInputItem(), AnvilRecipe.EnumTier.OBSIDIAN, AnvilRecipe.EnumType.HAMMER).getOutput().copy();
                                        outputStack.setCount(16);
                                        ItemHandlerHelper.insertItemStacked(itemHandler, outputStack, false);
                                    }
                                    this.getInputItem().shrink(1);
                                    hammerer.setTimeHeld(0);
                                    hammerer.setMustBeReset(true);
                                }
                            }
                            System.out.println(hammerer.getHammererRecipe().getInput());
                            if (!hammerer.getHammererRecipe().getInput().apply(this.getInputItem()) || hammerer.getPower() < 20D) {
                                hammerer.setTimeHeld(0);
                                hammerer.setHammererRecipe(null);
                                hammerer.setMustBeReset(true);
                                System.out.println("reset");
                            }
                        }
                    }
                }
            }
        }
    }

    public ItemStack getOutpuItem() {
        IItemHandler itemHandler = this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (itemHandler != null) return itemHandler.getStackInSlot(1);
        return null;
    }
}
