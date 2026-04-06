package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualRecipeBase;
import anightdazingzoroark.prift.helper.RiftUtil;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.ModuleTechBloomery;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.BloomeryRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.BloomeryRecipeBase;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.WitherForgeRecipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class TileEntitySemiManualHammererTop extends TileEntitySemiManualTopBase {
    public BloomeryRecipeBase<?> getHammererRecipe() {
        String hammererRecipeId = this.getCurrentRecipeId();
        if (ModuleTechBloomery.Registries.BLOOMERY_RECIPE.getValue(new ResourceLocation(hammererRecipeId)) != null) {
            return ModuleTechBloomery.Registries.BLOOMERY_RECIPE.getValue(new ResourceLocation(hammererRecipeId));
        }
        else if (ModuleTechBloomery.Registries.WITHER_FORGE_RECIPE.getValue(new ResourceLocation(hammererRecipeId)) != null) {
            return ModuleTechBloomery.Registries.WITHER_FORGE_RECIPE.getValue(new ResourceLocation(hammererRecipeId));
        }
        return null;
    }

    public double getMinPower() {
        BloomeryRecipeBase<?> currentRecipe = this.getHammererRecipe();
        if (currentRecipe instanceof BloomeryRecipe) return 20D;
        if (currentRecipe instanceof WitherForgeRecipe) return 40D;
        return -1D;
    }

    public int getMaxHammererTime() {
        BloomeryRecipeBase<?> currentRecipe = this.getHammererRecipe();
        //this is essentially getMaxRecipeTime() but minPower is equal to 20
        if (currentRecipe != null) {
            if (this.getPower() >= 20D) {
                double result = RiftUtil.slopeResult(this.getPower(), true, this.getMinPower(), this.getMinPower() * 8D, 30D, 5D);
                return (int) (result * 20);
            }
        }
        return -1;
    }

    @Override
    public SemiManualRecipeBase getCurrentRecipe() {
        return null;
    }
}
