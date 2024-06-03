package anightdazingzoroark.prift.compat.mysticalmechanics.items;

import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.BlockSemiManualBase;
import anightdazingzoroark.prift.compat.mysticalmechanics.blocks.RiftMMBlocks;
import anightdazingzoroark.prift.server.items.RiftItems;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class RiftMMItems {
    public static Item BLOW_POWERED_TURBINE;
    public static Item TURBINE;
    public static Item TURBINE_BLADE;
    public static Item SEMI_MANUAL_EXTRACTOR;

    public static void registerMMItems() {
        BLOW_POWERED_TURBINE = riftBlowPoweredTurbineItem("blow_powered_turbine");
        TURBINE = RiftItems.riftGenericItem("turbine", false);
        TURBINE_BLADE = RiftItems.riftGenericItem("turbine_blade", false);
        SEMI_MANUAL_EXTRACTOR = riftSemiManualPlacerItem("semi_manual_extractor", RiftMMBlocks.SEMI_MANUAL_EXTRACTOR, RiftMMBlocks.SEMI_MANUAL_EXTRACTOR_TOP);
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
