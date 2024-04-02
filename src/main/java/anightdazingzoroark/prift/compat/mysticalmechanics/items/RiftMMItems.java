package anightdazingzoroark.prift.compat.mysticalmechanics.items;

import anightdazingzoroark.prift.server.items.RiftItems;
import net.minecraft.item.Item;

public class RiftMMItems {
    public static Item BLOW_POWERED_TURBINE;
    public static Item TURBINE;
    public static Item TURBINE_BLADE;

    public static void registerMMItems() {
        BLOW_POWERED_TURBINE = riftBlowPoweredTurbineItem("blow_powered_turbine");
        TURBINE = RiftItems.riftGenericItem("turbine", false);
        TURBINE_BLADE = RiftItems.riftGenericItem("turbine_blade", false);
    }

    public static Item riftBlowPoweredTurbineItem(String registryName) {
        final RiftBlowPoweredTurbinePlacer item = new RiftBlowPoweredTurbinePlacer();
        return RiftItems.registerItem(item, registryName);
    }
}
