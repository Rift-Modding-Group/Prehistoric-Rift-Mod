package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.RiftUtil;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.ModuleTechBloomery;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.BloomeryRecipeBase;
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

    public int getMaxHammererTime() {
        //this is essentially getMaxRecipeTime() but minPower is equal to 20
        if (this.currentRecipe != null) {
            if (20D <= this.getPower()) {
                double result = -10D / 140D * (this.getPower() - 20D) + 15D;
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
        this.currentRecipe = ModuleTechBloomery.Registries.BLOOMERY_RECIPE.getValue(new ResourceLocation(compound.getString("currentRecipe")));
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        this.currentRecipe = ModuleTechBloomery.Registries.BLOOMERY_RECIPE.getValue(new ResourceLocation(tag.getString("currentRecipe")));
    }
}
