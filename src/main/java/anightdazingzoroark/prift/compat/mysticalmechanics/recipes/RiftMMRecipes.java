package anightdazingzoroark.prift.compat.mysticalmechanics.recipes;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.items.RiftMMItems;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.fluids.RiftFluids;
import anightdazingzoroark.prift.server.items.RiftItems;
import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import com.codetaylor.mc.pyrotech.modules.core.block.BlockRock;
import com.codetaylor.mc.pyrotech.modules.core.item.ItemRock;
import com.pam.harvestcraft.blocks.CropRegistry;
import com.pam.harvestcraft.blocks.FruitRegistry;
import com.pam.harvestcraft.config.ConfigHandler;
import com.pam.harvestcraft.item.ItemRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.oredict.OreDictionary;

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

    @Optional.Method(modid = RiftInitialize.HARVESTCRAFT_MOD_ID)
    public static void registerHarvestCraftRecipes() {
        //for millstone
        //make flour
        if (ConfigHandler.makeWheatEdible) millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/wheatToFlour"), Ingredient.fromStacks(new ItemStack(Items.WHEAT)), Ingredient.fromStacks(new ItemStack(ItemRegistry.flourItem)), 10));
        else millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/wheatToFlour"), Ingredient.fromStacks(new ItemStack(ItemRegistry.harvestwheatItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.flourItem)), 10));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/soybeanToFlour"), Ingredient.fromStacks(new ItemStack(CropRegistry.getFood(CropRegistry.SOYBEAN))), Ingredient.fromStacks(new ItemStack(ItemRegistry.flourItem)), 10));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/ryeToFlour"), Ingredient.fromStacks(new ItemStack(CropRegistry.getFood(CropRegistry.RYE))), Ingredient.fromStacks(new ItemStack(ItemRegistry.flourItem)), 10));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/riceToFlour"), Ingredient.fromStacks(new ItemStack(CropRegistry.getFood(CropRegistry.RICE))), Ingredient.fromStacks(new ItemStack(ItemRegistry.flourItem)), 10));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/quinoaToFlour"), Ingredient.fromStacks(new ItemStack(CropRegistry.getFood(CropRegistry.QUINOA))), Ingredient.fromStacks(new ItemStack(ItemRegistry.flourItem)), 10));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/potatoToFlour"), Ingredient.fromStacks(new ItemStack(ItemRegistry.harvestpotatoItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.flourItem)), 10));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/peasToFlour"), Ingredient.fromStacks(new ItemStack(CropRegistry.getFood(CropRegistry.PEAS))), Ingredient.fromStacks(new ItemStack(ItemRegistry.flourItem)), 10));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/oatsToFlour"), Ingredient.fromStacks(new ItemStack(CropRegistry.getFood(CropRegistry.OATS))), Ingredient.fromStacks(new ItemStack(ItemRegistry.flourItem)), 10));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/milletToFlour"), Ingredient.fromStacks(new ItemStack(CropRegistry.getFood(CropRegistry.MILLET))), Ingredient.fromStacks(new ItemStack(ItemRegistry.flourItem)), 10));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/coconutToFlour"), Ingredient.fromStacks(new ItemStack(FruitRegistry.getFood(FruitRegistry.COCONUT))), Ingredient.fromStacks(new ItemStack(ItemRegistry.flourItem)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/chickpeaToFlour"), Ingredient.fromStacks(new ItemStack(CropRegistry.getFood(CropRegistry.CHICKPEA))), Ingredient.fromStacks(new ItemStack(ItemRegistry.flourItem)), 10));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/chestnutToFlour"), Ingredient.fromStacks(new ItemStack(FruitRegistry.getFood(FruitRegistry.CHESTNUT))), Ingredient.fromStacks(new ItemStack(ItemRegistry.flourItem)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/cassavaToFlour"), Ingredient.fromStacks(new ItemStack(CropRegistry.getFood(CropRegistry.CASSAVA))), Ingredient.fromStacks(new ItemStack(ItemRegistry.flourItem)), 10));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/beanToFlour"), Ingredient.fromStacks(new ItemStack(CropRegistry.getFood(CropRegistry.BEAN))), Ingredient.fromStacks(new ItemStack(ItemRegistry.flourItem)), 10));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/barleyToFlour"), Ingredient.fromStacks(new ItemStack(CropRegistry.getFood(CropRegistry.BARLEY))), Ingredient.fromStacks(new ItemStack(ItemRegistry.flourItem)), 10));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/bananaToFlour"), Ingredient.fromStacks(new ItemStack(FruitRegistry.getFood(FruitRegistry.BANANA))), Ingredient.fromStacks(new ItemStack(ItemRegistry.flourItem)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/amaranthToFlour"), Ingredient.fromStacks(new ItemStack(CropRegistry.getFood(CropRegistry.AMARANTH))), Ingredient.fromStacks(new ItemStack(ItemRegistry.flourItem)), 10));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/almondToFlour"), Ingredient.fromStacks(new ItemStack(FruitRegistry.getFood(FruitRegistry.ALMOND))), Ingredient.fromStacks(new ItemStack(ItemRegistry.flourItem)), 20));

        //make ground meat
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/makeGroundChicken"), Ingredient.fromStacks(new ItemStack(ItemRegistry.harvestchickenItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundchickenItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/makeGroundPorkchop"), Ingredient.fromStacks(new ItemStack(ItemRegistry.harvestporkchopItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundporkItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/makeGroundBeef"), Ingredient.fromStacks(new ItemStack(ItemRegistry.harvestbeefItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundbeefItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/makeGroundMutton"), Ingredient.fromStacks(new ItemStack(ItemRegistry.harvestmuttonItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundmuttonItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/vFishToGroundFish"), Ingredient.fromStacks(new ItemStack(Items.FISH, 1, 0)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/vSalmonToGroundFish"), Ingredient.fromStacks(new ItemStack(Items.FISH, 1, 1)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/vClownfishToGroundFish"), Ingredient.fromStacks(new ItemStack(Items.FISH, 1, 2)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));

        //make ground fish
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/fishToGroundFish"), Ingredient.fromStacks(new ItemStack(ItemRegistry.harvestfishItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/salmonToGroundFish"), Ingredient.fromStacks(new ItemStack(ItemRegistry.harvestsalmonItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/clownfishToGroundFish"), Ingredient.fromStacks(new ItemStack(ItemRegistry.harvestclownfishItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/calamariToGroundFish"), Ingredient.fromStacks(new ItemStack(ItemRegistry.calamarirawItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/anchovyToGroundFish"), Ingredient.fromStacks(new ItemStack(ItemRegistry.anchovyrawItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/bassToGroundFish"), Ingredient.fromStacks(new ItemStack(ItemRegistry.bassrawItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/carpToGroundFish"), Ingredient.fromStacks(new ItemStack(ItemRegistry.carprawItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/catfishToGroundFish"), Ingredient.fromStacks(new ItemStack(ItemRegistry.catfishrawItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/charrToGroundFish"), Ingredient.fromStacks(new ItemStack(ItemRegistry.charrrawItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/grouperToGroundFish"), Ingredient.fromStacks(new ItemStack(ItemRegistry.grouperrawItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/herringToGroundFish"), Ingredient.fromStacks(new ItemStack(ItemRegistry.herringrawItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/mudfishToGroundFish"), Ingredient.fromStacks(new ItemStack(ItemRegistry.mudfishrawItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/perchToGroundFish"), Ingredient.fromStacks(new ItemStack(ItemRegistry.perchrawItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/snapperToGroundFish"), Ingredient.fromStacks(new ItemStack(ItemRegistry.snapperrawItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/tilapiaToGroundFish"), Ingredient.fromStacks(new ItemStack(ItemRegistry.tilapiarawItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/troutToGroundFish"), Ingredient.fromStacks(new ItemStack(ItemRegistry.troutrawItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/tunaToGroundFish"), Ingredient.fromStacks(new ItemStack(ItemRegistry.tunarawItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/walleyeToGroundFish"), Ingredient.fromStacks(new ItemStack(ItemRegistry.walleyerawItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/greenHeartFishToGroundFish"), Ingredient.fromStacks(new ItemStack(ItemRegistry.greenheartfishItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/sardineToGroundFish"), Ingredient.fromStacks(new ItemStack(ItemRegistry.sardinerawItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
        millstoneRecipes.add(new MillstoneRecipe(new ResourceLocation(RiftInitialize.MODID, "millstone/tofishToGroundFish"), Ingredient.fromStacks(new ItemStack(ItemRegistry.rawtofishItem)), Ingredient.fromStacks(new ItemStack(ItemRegistry.groundfishItem, 2)), 20));
    }
}
