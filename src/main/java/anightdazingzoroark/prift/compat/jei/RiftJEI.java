package anightdazingzoroark.prift.compat.jei;

import anightdazingzoroark.prift.client.ui.RiftSemiManualExtractorMenu;
import anightdazingzoroark.prift.client.ui.RiftSemiManualExtruderMenu;
import anightdazingzoroark.prift.client.ui.RiftSemiManualPresserMenu;
import anightdazingzoroark.prift.compat.jei.category.RiftJEISMExtractorCategory;
import anightdazingzoroark.prift.compat.jei.category.RiftJEISMExtruderCategory;
import anightdazingzoroark.prift.compat.jei.category.RiftJEISMPresserCategory;
import anightdazingzoroark.prift.compat.jei.wrapper.RiftJEISMExtractorWrapper;
import anightdazingzoroark.prift.compat.jei.wrapper.RiftJEISMExtruderWrapper;
import anightdazingzoroark.prift.compat.jei.wrapper.RiftJEISMPresserWrapper;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualExtractorRecipe;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualExtruderRecipe;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualPresserRecipe;
import anightdazingzoroark.prift.server.entity.projectile.RiftProjectiles;
import anightdazingzoroark.prift.server.items.RiftItems;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@JEIPlugin
public class RiftJEI implements IModPlugin {
    public static final String smExtractorCat = "prift.semi_manual_extractor";
    public static final String smPresserCat = "prift.semi_manual_presser";
    public static final String smExtruderCat = "prift.semi_manual_extruder";

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

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        final IJeiHelpers helpers = registry.getJeiHelpers();
        final IGuiHelper gui = helpers.getGuiHelper();
        registry.addRecipeCategories(new RiftJEISMExtractorCategory(gui));
        registry.addRecipeCategories(new RiftJEISMPresserCategory(gui));
        registry.addRecipeCategories(new RiftJEISMExtruderCategory(gui));
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

        //add the custom recipes
        registry.addRecipes(this.semiManualExtractorWrappers(), smExtractorCat);
        registry.addRecipeClickArea(RiftSemiManualExtractorMenu.class, 64, 33, 21, 14, smExtractorCat);

        registry.addRecipes(this.semiManualPresserWrappers(), smPresserCat);
        registry.addRecipeClickArea(RiftSemiManualPresserMenu.class, 74, 33, 21, 14, smPresserCat);

        registry.addRecipes(this.semiManualExtruderWrappers(), smExtruderCat);
        registry.addRecipeClickArea(RiftSemiManualExtruderMenu.class, 74, 33, 21, 14, smExtruderCat);
    }
}
