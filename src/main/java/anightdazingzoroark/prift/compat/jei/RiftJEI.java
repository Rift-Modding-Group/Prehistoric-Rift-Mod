package anightdazingzoroark.prift.compat.jei;

import anightdazingzoroark.prift.client.ui.*;
import anightdazingzoroark.prift.compat.jei.category.*;
import anightdazingzoroark.prift.compat.jei.wrapper.*;
import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.RiftMMBlocks;
import anightdazingzoroark.prift.compat.mysticalmechanics.items.RiftMMItems;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.*;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.blocks.RiftBlocks;
import anightdazingzoroark.prift.server.entity.projectile.RiftProjectileRegistry;
import anightdazingzoroark.prift.server.items.RiftItems;
import com.codetaylor.mc.pyrotech.ModPyrotechConfig;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.ModuleTechBloomery;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.BloomeryRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.WitherForgeRecipe;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import static com.codetaylor.mc.pyrotech.modules.tech.bloomery.ModuleTechBloomery.Registries.BLOOMERY_RECIPE;
import static com.codetaylor.mc.pyrotech.modules.tech.bloomery.ModuleTechBloomery.Registries.WITHER_FORGE_RECIPE;

@JEIPlugin
public class RiftJEI implements IModPlugin {
    private static IJeiRuntime jeiRuntime;
    public static final String smExtractorCat = "prift.semi_manual_extractor";
    public static final String smPresserCat = "prift.semi_manual_presser";
    public static final String smExtruderCat = "prift.semi_manual_extruder";
    public static final String smHammererCat = "prift.semi_manual_hammerer";
    public static final String millstoneCat = "prift.millstone";
    public static final String mechFilterCat = "prift.mechanical_filter";

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
        RiftJEI.jeiRuntime = jeiRuntime;
    }

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

    private List<RiftJEIMillstoneWrapper> millstoneWrappers() {
        List<RiftJEIMillstoneWrapper> list = new ArrayList<>();
        for (MillstoneRecipe recipe : RiftMMRecipes.millstoneRecipes) {
            list.add(new RiftJEIMillstoneWrapper(recipe));
        }
        return list;
    }

    private List<RiftJEIMechanicalFilterWrapper> mechanicalFilterWrappers() {
        List<RiftJEIMechanicalFilterWrapper> list = new ArrayList<>();
        for (MechanicalFilterRecipe recipe : RiftMMRecipes.mechanicalFilterRecipes) {
            for (MechanicalFilterRecipe.MechanicalFilterOutput output : recipe.output) {
                list.add(new RiftJEIMechanicalFilterWrapper(recipe, output));
            }
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
            registry.addRecipeCategories(new RiftJEIMillstoneCategory(gui));
            registry.addRecipeCategories(new RiftJEIMechanicalFilterCategory(gui));
        }
    }

    @Override
    public void register(IModRegistry registry) {
        //hide the projectile animators from jei
        IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectileRegistry.CANNONBALL));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectileRegistry.MORTAR_SHELL));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectileRegistry.CATAPULT_BOULDER));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectileRegistry.THROWN_STEGOSAURUS_PLATE_ONE));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectileRegistry.THROWN_STEGOSAURUS_PLATE_TWO));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectileRegistry.THROWN_STEGOSAURUS_PLATE_THREE));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectileRegistry.THROWN_STEGOSAURUS_PLATE_FOUR));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectileRegistry.THROWN_BOLA));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectileRegistry.DILOPHOSAURUS_SPIT));

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

            registry.addRecipes(this.millstoneWrappers(), millstoneCat);
            registry.addRecipeClickArea(RiftMillstoneMenu.class, 81, 44, 14, 22, millstoneCat);
            registry.addRecipeCatalyst(new ItemStack(RiftMMBlocks.MILLSTONE), millstoneCat);

            registry.addRecipes(this.mechanicalFilterWrappers(), mechFilterCat);
            registry.addRecipeClickArea(RiftMechanicalFilterMenu.class, 81, 44, 14, 22, mechFilterCat);
            registry.addRecipeCatalyst(new ItemStack(RiftMMBlocks.MECHANICAL_FILTER), mechFilterCat);
        }
    }

    public static boolean showRecipesForItemStack(ItemStack itemStack, boolean isUses) {
        jeiRuntime.getRecipesGui().show(jeiRuntime.getRecipeRegistry().createFocus(isUses ? IFocus.Mode.INPUT : IFocus.Mode.OUTPUT, itemStack));
        return Minecraft.getMinecraft().currentScreen instanceof IRecipesGui;
    }
}
