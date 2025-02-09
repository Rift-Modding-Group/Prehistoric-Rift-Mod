package anightdazingzoroark.prift.compat.mysticalmechanics.items;

import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.RiftMMBlocks;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.items.RiftItems;
import com.codetaylor.mc.pyrotech.ModPyrotechConfig;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.ModuleTechBloomery;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.oredict.OreDictionary;

public class RiftMMItems {
    public static Item BLOW_POWERED_TURBINE;
    public static Item TURBINE;
    public static Item TURBINE_BLADE;
    public static Item SEMI_MANUAL_MACHINE_FRAME;
    public static Item SEMI_MANUAL_EXTRACTOR_CORE;
    public static Item SEMI_MANUAL_PRESSER_CORE;
    public static Item SEMI_MANUAL_EXTRUDER_CORE;
    public static Item SEMI_MANUAL_HAMMERER_CORE;
    public static Item SEMI_MANUAL_EXTRACTOR;
    public static Item SEMI_MANUAL_PRESSER;
    public static Item SEMI_MANUAL_EXTRUDER;
    public static Item SEMI_MANUAL_HAMMERER;
    public static Item COPPER_INGOT;
    public static Item COPPER_NUGGET;
    public static Item COPPER_PLATE;
    public static Item IRON_PLATE;
    public static Item GOLD_PLATE;
    public static Item COPPER_ROD;
    public static Item IRON_ROD;
    public static Item GOLD_ROD;
    public static Item HAMMER;
    public static Item FILE;

    public static void registerMMItems() {
        BLOW_POWERED_TURBINE = riftBlowPoweredTurbineItem("blow_powered_turbine");
        TURBINE = RiftItems.riftGenericItem("turbine", false);
        TURBINE_BLADE = RiftItems.riftGenericItem("turbine_blade", false);
        SEMI_MANUAL_MACHINE_FRAME = RiftItems.riftGenericItem("semi_manual_machine_frame", false);
        SEMI_MANUAL_EXTRACTOR_CORE = RiftItems.riftGenericItem("semi_manual_extractor_core", false);
        SEMI_MANUAL_PRESSER_CORE = RiftItems.riftGenericItem("semi_manual_presser_core", false);
        SEMI_MANUAL_EXTRUDER_CORE = RiftItems.riftGenericItem("semi_manual_extruder_core", false);
        if (GeneralConfig.canUsePyrotech() && ModPyrotechConfig.MODULES.get(ModuleTechBloomery.MODULE_ID)) SEMI_MANUAL_HAMMERER_CORE = RiftItems.riftGenericItem("semi_manual_hammerer_core", false);
        SEMI_MANUAL_EXTRACTOR = riftSemiManualPlacerItem("semi_manual_extractor", RiftMMBlocks.SEMI_MANUAL_EXTRACTOR, RiftMMBlocks.SEMI_MANUAL_EXTRACTOR_TOP);
        SEMI_MANUAL_PRESSER = riftSemiManualPlacerItem("semi_manual_presser", RiftMMBlocks.SEMI_MANUAL_PRESSER, RiftMMBlocks.SEMI_MANUAL_PRESSER_TOP);
        SEMI_MANUAL_EXTRUDER = riftSemiManualPlacerItem("semi_manual_extruder", RiftMMBlocks.SEMI_MANUAL_EXTRUDER, RiftMMBlocks.SEMI_MANUAL_EXTRUDER_TOP);
        if (GeneralConfig.canUsePyrotech() && ModPyrotechConfig.MODULES.get(ModuleTechBloomery.MODULE_ID)) SEMI_MANUAL_HAMMERER = riftSemiManualPlacerItem("semi_manual_hammerer", RiftMMBlocks.SEMI_MANUAL_HAMMERER, RiftMMBlocks.SEMI_MANUAL_HAMMERER_TOP);
        IRON_PLATE = RiftItems.riftGenericItem("iron_plate", true);
        GOLD_PLATE = RiftItems.riftGenericItem("gold_plate", true);
        IRON_ROD = RiftItems.riftGenericItem("iron_rod", true);
        GOLD_ROD = RiftItems.riftGenericItem("gold_rod", true);
        HAMMER = RiftItems.registerItem(new RiftMMCraftingTool(), "hammer", true);
        FILE = RiftItems.registerItem(new RiftMMCraftingTool(), "file", true);
    }

    public static void registerOreDicTags() {
        OreDictionary.registerOre("plateIron", IRON_PLATE);
        OreDictionary.registerOre("plateGold", GOLD_PLATE);
        OreDictionary.registerOre("stickIron", IRON_ROD);
        OreDictionary.registerOre("stickGold", GOLD_ROD);
    }

    public static Item riftBlowPoweredTurbineItem(String registryName) {
        final RiftBlowPoweredTurbinePlacer item = new RiftBlowPoweredTurbinePlacer();
        return RiftItems.registerItem(item, registryName, true);
    }

    public static Item riftSemiManualPlacerItem(String registryName, Block blockBottom, Block blockTop) {
        final RiftSemiManualPlacerBase item = new RiftSemiManualPlacerBase(blockBottom, blockTop);
        return RiftItems.registerItem(item, registryName, true);
    }
}
