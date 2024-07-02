package anightdazingzoroark.prift.compat.jei;

import anightdazingzoroark.prift.client.ui.RiftSemiManualExtractorMenu;
import anightdazingzoroark.prift.client.ui.RiftSemiManualExtruderMenu;
import anightdazingzoroark.prift.client.ui.RiftSemiManualHammererMenu;
import anightdazingzoroark.prift.client.ui.RiftSemiManualPresserMenu;
import anightdazingzoroark.prift.compat.jei.category.RiftJEISMExtractorCategory;
import anightdazingzoroark.prift.compat.jei.category.RiftJEISMExtruderCategory;
import anightdazingzoroark.prift.compat.jei.category.RiftJEISMHammererCategory;
import anightdazingzoroark.prift.compat.jei.category.RiftJEISMPresserCategory;
import anightdazingzoroark.prift.compat.jei.wrapper.RiftJEISMExtractorWrapper;
import anightdazingzoroark.prift.compat.jei.wrapper.RiftJEISMExtruderWrapper;
import anightdazingzoroark.prift.compat.jei.wrapper.RiftJEISMHammererWrapper;
import anightdazingzoroark.prift.compat.jei.wrapper.RiftJEISMPresserWrapper;
import anightdazingzoroark.prift.compat.mysticalmechanics.items.RiftMMItems;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualExtractorRecipe;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualExtruderRecipe;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualPresserRecipe;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.blocks.RiftBlocks;
import anightdazingzoroark.prift.server.entity.projectile.RiftProjectiles;
import anightdazingzoroark.prift.server.items.RiftItems;
import com.codetaylor.mc.pyrotech.ModPyrotechConfig;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.ModuleTechBloomery;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.BloomeryRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.WitherForgeRecipe;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import static com.codetaylor.mc.pyrotech.modules.tech.bloomery.ModuleTechBloomery.Registries.BLOOMERY_RECIPE;
import static com.codetaylor.mc.pyrotech.modules.tech.bloomery.ModuleTechBloomery.Registries.WITHER_FORGE_RECIPE;

@JEIPlugin
public class RiftJEI implements IModPlugin {
    public static final String smExtractorCat = "prift.semi_manual_extractor";
    public static final String smPresserCat = "prift.semi_manual_presser";
    public static final String smExtruderCat = "prift.semi_manual_extruder";
    public static final String smHammererCat = "prift.semi_manual_hammerer";

    //recipe makers
    private List<RiftJEISMExtractorWrapper> semiManualExtractorWrappers() {
        List<RiftJEISMExtractorWrapper> list = new ArrayList<>();
        for (SemiManualExtractorRecipe recipe : RiftMMRecipes.smExtractorRecipes) {
            list.add(new RiftJEISMExtractorWrapper(recipe));
        }
        return list;
    }

    private List<RiftJEISMPresserWrapper> semiManualPresserWrappers() {
        List<RiftJEISMPresserWrapper> list = new ArrayList<>();
        for (SemiManualPresserRecipe recipe : RiftMMRecipes.smPresserRecipes) {
            list.add(new RiftJEISMPresserWrapper(recipe));
        }
        return list;
    }

    private List<RiftJEISMExtruderWrapper> semiManualExtruderWrappers() {
        List<RiftJEISMExtruderWrapper> list = new ArrayList<>();
        for (SemiManualExtruderRecipe recipe : RiftMMRecipes.smExtruderRecipes) {
            list.add(new RiftJEISMExtruderWrapper(recipe));
        }
        return list;
    }

    private List<RiftJEISMHammererWrapper> semiManualHammererWrappers() {
        List<RiftJEISMHammererWrapper> list = new ArrayList<>();
        for (Entry<ResourceLocation, BloomeryRecipe> bloomeryRecipe: BLOOMERY_RECIPE.getEntries()) {
            list.add(new RiftJEISMHammererWrapper(bloomeryRecipe.getValue()));
        }
        for (Entry<ResourceLocation, WitherForgeRecipe> witherForgeRecipe: WITHER_FORGE_RECIPE.getEntries()) {
            list.add(new RiftJEISMHammererWrapper(witherForgeRecipe.getValue()));
        }
        return list;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        final IJeiHelpers helpers = registry.getJeiHelpers();
        final IGuiHelper gui = helpers.getGuiHelper();
        if (GeneralConfig.canUseMM()) {
            registry.addRecipeCategories(new RiftJEISMExtractorCategory(gui));
            registry.addRecipeCategories(new RiftJEISMPresserCategory(gui));
            registry.addRecipeCategories(new RiftJEISMExtruderCategory(gui));
            if (GeneralConfig.canUsePyrotech() && ModPyrotechConfig.MODULES.get(ModuleTechBloomery.MODULE_ID)) registry.addRecipeCategories(new RiftJEISMHammererCategory(gui));
        }
    }

    @Override
    public void register(IModRegistry registry) {
        //hide the projectile animators from jei
        IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectiles.CANNONBALL));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectiles.MORTAR_SHELL));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectiles.CATAPULT_BOULDER));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectiles.THROWN_STEGOSAURUS_PLATE_ONE));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectiles.THROWN_STEGOSAURUS_PLATE_TWO));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectiles.THROWN_STEGOSAURUS_PLATE_THREE));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectiles.THROWN_STEGOSAURUS_PLATE_FOUR));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectiles.THROWN_BOLA));

        //hide le hidden items
        blacklist.addIngredientToBlacklist(new ItemStack(RiftItems.DETECT_ALERT));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftItems.CHEST_DETECT_ALERT));

        //hide fluids
        blacklist.addIngredientToBlacklist(new ItemStack(RiftBlocks.PYROBERRY_JUICE_FLUID));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftBlocks.CRYOBERRY_JUICE_FLUID));

        //add the custom recipes
        if (GeneralConfig.canUseMM()) {
            registry.addRecipes(this.semiManualExtractorWrappers(), smExtractorCat);
            registry.addRecipeClickArea(RiftSemiManualExtractorMenu.class, 64, 33, 21, 14, smExtractorCat);
            registry.addRecipeCatalyst(new ItemStack(RiftMMItems.SEMI_MANUAL_EXTRACTOR), smExtractorCat);

            registry.addRecipes(this.semiManualPresserWrappers(), smPresserCat);
            registry.addRecipeClickArea(RiftSemiManualPresserMenu.class, 74, 33, 21, 14, smPresserCat);
            registry.addRecipeCatalyst(new ItemStack(RiftMMItems.SEMI_MANUAL_PRESSER), smPresserCat);

            registry.addRecipes(this.semiManualExtruderWrappers(), smExtruderCat);
            registry.addRecipeClickArea(RiftSemiManualExtruderMenu.class, 74, 33, 21, 14, smExtruderCat);
            registry.addRecipeCatalyst(new ItemStack(RiftMMItems.SEMI_MANUAL_EXTRUDER), smExtruderCat);

            if (GeneralConfig.canUsePyrotech() && ModPyrotechConfig.MODULES.get(ModuleTechBloomery.MODULE_ID)) {
                registry.addRecipes(this.semiManualHammererWrappers(), smHammererCat);
                registry.addRecipeClickArea(RiftSemiManualHammererMenu.class, 58, 33, 21, 14, smHammererCat);
                registry.addRecipeCatalyst(new ItemStack(RiftMMItems.SEMI_MANUAL_HAMMERER), smHammererCat);
            }
        }
    }
}
