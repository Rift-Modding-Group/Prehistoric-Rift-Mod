package anightdazingzoroark.prift.compat.mysticalmechanics.recipes;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.items.RiftMMItems;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.fluids.RiftFluids;
import anightdazingzoroark.prift.server.items.RiftItems;
import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import com.codetaylor.mc.pyrotech.modules.core.block.BlockRock;
import com.codetaylor.mc.pyrotech.modules.core.item.ItemRock;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class RiftMMRecipes {
    public static List<SemiManualExtractorRecipe> smExtractorRecipes = new ArrayList<>();
    public static List<SemiManualPresserRecipe> smPresserRecipes = new ArrayList<>();
    public static List<SemiManualExtruderRecipe> smExtruderRecipes = new ArrayList<>();
    public static List<MillstoneRecipe> millstoneRecipes = new ArrayList<>();
    public static List<MechanicalFilterRecipe> mechanicalFilterRecipes = new ArrayList<>();

    public static SemiManualRecipeBase getSMRecipe(String path) {
        for (SemiManualExtractorRecipe recipe : smExtractorRecipes) {
            if (recipe.getId().equals(path)) return recipe;
        }
        for (SemiManualPresserRecipe recipe : smPresserRecipes) {
            if (recipe.getId().equals(path)) return recipe;
        }
        for (SemiManualExtruderRecipe recipe : smExtruderRecipes) {
            if (recipe.getId().equals(path)) return recipe;
        }
        return null;
    }

    public static SemiManualExtractorRecipe getSMExtractorRecipe(String path) {
        for (SemiManualExtractorRecipe recipe : smExtractorRecipes) {
            if (recipe.getId().equals(path)) return recipe;
        }
        return null;
    }

    public static SemiManualPresserRecipe getSMPresserRecipe(String path) {
        for (SemiManualPresserRecipe recipe : smPresserRecipes) {
            if (recipe.getId().equals(path)) return recipe;
        }
        return null;
    }

    public static SemiManualExtruderRecipe getSMExtruderRecipe(String path) {
        for (SemiManualExtruderRecipe recipe : smExtruderRecipes) {
            if (recipe.getId().equals(path)) return recipe;
        }
        return null;
    }

    public static MillstoneRecipe getMillstoneRecipe(String path) {
        for (MillstoneRecipe recipe : millstoneRecipes) {
            if (recipe.getId().equals(path)) return recipe;
        }
        return null;
    }

    public static MechanicalFilterRecipe getMechanicalFilterRecipe(String path) {
        for (MechanicalFilterRecipe recipe : mechanicalFilterRecipes) {
            if (recipe.getId().equals(path)) return recipe;
        }
        return null;
    }

    public static void registerRecipes() {
        //for semi manual extractor
        smExtractorRecipes.add(new SemiManualExtractorRecipe(new ResourceLocation(RiftInitialize.MODID, "sme/pyroberryToPyroberryJuice"), Ingredient.fromStacks(new ItemStack(RiftItems.PYROBERRY)), new FluidStack(RiftFluids.PYROBERRY_JUICE, 125), 10));
        smExtractorRecipes.add(new SemiManualExtractorRecipe(new ResourceLocation(RiftInitialize.MODID, "sme/cryoberryToCryoberryJuice"), Ingredient.fromStacks(new ItemStack(RiftItems.CRYOBERRY)), new FluidStack(RiftFluids.CRYOBERRY_JUICE, 125), 10));

        //for semi manual presser
        smPresserRecipes.add(new SemiManualPresserRecipe(new ResourceLocation(RiftInitialize.MODID, "smp/ironIngotToPlate"), Ingredient.fromStacks(new ItemStack(Items.IRON_INGOT)), Ingredient.fromStacks(new ItemStack(RiftMMItems.IRON_PLATE)), 20));
        smPresserRecipes.add(new SemiManualPresserRecipe(new ResourceLocation(RiftInitialize.MODID, "smp/goldIngotToPlate"), Ingredient.fromStacks(new ItemStack(Items.GOLD_INGOT)), Ingredient.fromStacks(new ItemStack(RiftMMItems.GOLD_PLATE)), 20));

        //for semi manual extruder
        smExtruderRecipes.add(new SemiManualExtruderRecipe(new ResourceLocation(RiftInitialize.MODID, "smex/ironIngotToRod"), Ingredient.fromStacks(new ItemStack(Items.IRON_INGOT)), Ingredient.fromStacks(new ItemStack(RiftMMItems.IRON_ROD, 2)), 20));
        smExtruderRecipes.add(new SemiManualExtruderRecipe(new ResourceLocation(RiftInitialize.MODID, "smex/goldIngotToRod"), Ingredient.fromStacks(new ItemStack(Items.GOLD_INGOT)), Ingredient.fromStacks(new ItemStack(RiftMMItems.GOLD_ROD, 2)), 20));

        //for millstone
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/caneToSugar"), Ingredient.fromStacks(new ItemStack(Items.REEDS)), Ingredient.fromStacks(new ItemStack(Items.SUGAR)), 10));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/boneToBonemeal"), Ingredient.fromStacks(new ItemStack(Items.BONE)), Ingredient.fromStacks(new ItemStack(Items.DYE, 2, 15)), 10));

        //for mechanical filter
        List<MechanicalFilterRecipe.MechanicalFilterOutput> dirtOutput = new ArrayList<>();
        dirtOutput.add(new MechanicalFilterRecipe.MechanicalFilterOutput(Ingredient.fromStacks(new ItemStack(Blocks.DIRT)), 20));
        dirtOutput.add(new MechanicalFilterRecipe.MechanicalFilterOutput(Ingredient.fromStacks(new ItemStack(Items.CARROT, 2)), 10));
        dirtOutput.add(new MechanicalFilterRecipe.MechanicalFilterOutput(Ingredient.fromStacks(new ItemStack(Items.POTATO, 2)), 10));
        dirtOutput.add(new MechanicalFilterRecipe.MechanicalFilterOutput(Ingredient.fromStacks(new ItemStack(RiftItems.TRUFFLE, 2)), 5));
        mechanicalFilterRecipes.add(new MechanicalFilterRecipe(new ResourceLocation(RiftInitialize.MODID, "mechanicalFilter/fromDirt"), Ingredient.fromStacks(new ItemStack(Blocks.DIRT)), dirtOutput, 5));

        List<MechanicalFilterRecipe.MechanicalFilterOutput> gravelOutput = new ArrayList<>();
        gravelOutput.add(new MechanicalFilterRecipe.MechanicalFilterOutput(Ingredient.fromStacks(new ItemStack(Blocks.GRAVEL)), 20));
        gravelOutput.add(new MechanicalFilterRecipe.MechanicalFilterOutput(Ingredient.fromStacks(new ItemStack(Items.FLINT)), 10));
        gravelOutput.add(new MechanicalFilterRecipe.MechanicalFilterOutput(Ingredient.fromStacks(new ItemStack(Items.IRON_NUGGET, 3)), 5));
        mechanicalFilterRecipes.add(new MechanicalFilterRecipe(new ResourceLocation(RiftInitialize.MODID, "mechanicalFilter/fromGravel"), Ingredient.fromStacks(new ItemStack(Blocks.GRAVEL)), gravelOutput, 5));

        List<MechanicalFilterRecipe.MechanicalFilterOutput> sandOutput = new ArrayList<>();
        sandOutput.add(new MechanicalFilterRecipe.MechanicalFilterOutput(Ingredient.fromStacks(new ItemStack(Blocks.SAND)), 20));
        sandOutput.add(new MechanicalFilterRecipe.MechanicalFilterOutput(Ingredient.fromStacks(new ItemStack(Items.DYE, 3, 4)), 5));
        mechanicalFilterRecipes.add(new MechanicalFilterRecipe(new ResourceLocation(RiftInitialize.MODID, "mechanicalFilter/fromSand"), Ingredient.fromStacks(new ItemStack(Blocks.SAND)), sandOutput, 5));

        List<MechanicalFilterRecipe.MechanicalFilterOutput> redSandOutput = new ArrayList<>();
        redSandOutput.add(new MechanicalFilterRecipe.MechanicalFilterOutput(Ingredient.fromStacks(new ItemStack(Blocks.SAND, 1, 1)), 20));
        redSandOutput.add(new MechanicalFilterRecipe.MechanicalFilterOutput(Ingredient.fromStacks(new ItemStack(Items.GOLD_NUGGET, 3)), 5));
        mechanicalFilterRecipes.add(new MechanicalFilterRecipe(new ResourceLocation(RiftInitialize.MODID, "mechanicalFilter/fromRedSand"), Ingredient.fromStacks(new ItemStack(Blocks.SAND, 1, 1)), redSandOutput, 5));
    }
}
