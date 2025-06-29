package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.helper.RiftUtil;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.ModuleTechBloomery;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.BloomeryRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.BloomeryRecipeBase;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.WitherForgeRecipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class TileEntitySemiManualHammererTop extends TileEntitySemiManualTopBase {
    private BloomeryRecipeBase<?> currentRecipe;

    public BloomeryRecipeBase<?> getHammererRecipe() {
        return this.currentRecipe;
    }

    public String getHammererRecipeId() {
        if (this.currentRecipe != null) return this.currentRecipe.getRegistryName().toString();
        return "";
    }

    public void setHammererRecipe(BloomeryRecipeBase<?> value) {
        this.currentRecipe = value;
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public double getMinPower() {
        if (this.currentRecipe instanceof BloomeryRecipe) return 20D;
        if (this.currentRecipe instanceof WitherForgeRecipe) return 40D;
        return -1D;
    }

    public int getMaxHammererTime() {
        //this is essentially getMaxRecipeTime() but minPower is equal to 20
        if (this.currentRecipe != null) {
            if (20D <= this.getPower()) {
                double result = -10D / (7D + this.getMinPower()) * (this.getPower() - this.getMinPower()) + 15D;
                return (int) RiftUtil.clamp(result, 5D, 30D) * 20;
            }
        }
        return -1;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString("currentRecipe", this.getHammererRecipeId());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (ModuleTechBloomery.Registries.BLOOMERY_RECIPE.getValue(new ResourceLocation(compound.getString("currentRecipe"))) != null) {
            this.currentRecipe = ModuleTechBloomery.Registries.BLOOMERY_RECIPE.getValue(new ResourceLocation(compound.getString("currentRecipe")));
        }
        else if (ModuleTechBloomery.Registries.WITHER_FORGE_RECIPE.getValue(new ResourceLocation(compound.getString("currentRecipe"))) != null) {
            this.currentRecipe = ModuleTechBloomery.Registries.WITHER_FORGE_RECIPE.getValue(new ResourceLocation(compound.getString("currentRecipe")));
        }
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        if (ModuleTechBloomery.Registries.BLOOMERY_RECIPE.getValue(new ResourceLocation(tag.getString("currentRecipe"))) != null) {
            this.currentRecipe = ModuleTechBloomery.Registries.BLOOMERY_RECIPE.getValue(new ResourceLocation(tag.getString("currentRecipe")));
        }
        else if (ModuleTechBloomery.Registries.WITHER_FORGE_RECIPE.getValue(new ResourceLocation(tag.getString("currentRecipe"))) != null) {
            this.currentRecipe = ModuleTechBloomery.Registries.WITHER_FORGE_RECIPE.getValue(new ResourceLocation(tag.getString("currentRecipe")));
        }
    }
}
