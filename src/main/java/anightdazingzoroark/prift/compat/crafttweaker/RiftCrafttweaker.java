package anightdazingzoroark.prift.compat.crafttweaker;

import anightdazingzoroark.prift.config.GeneralConfig;
import crafttweaker.CraftTweakerAPI;
import net.minecraftforge.fml.common.Loader;

public class RiftCrafttweaker {
    public static final String CRAFTTWEAKER_MOD_ID = "crafttweaker";

    public static void loadCrafttweakerCompat() {
        if (Loader.isModLoaded(CRAFTTWEAKER_MOD_ID)) {
            if (GeneralConfig.canUseMM()) {
                CraftTweakerAPI.registerClass(RiftCrafttweakerSemiManualExtractor.class);
                CraftTweakerAPI.registerClass(RiftCrafttweakerSemiManualPresser.class);
                CraftTweakerAPI.registerClass(RiftCrafttweakerSemiManualExtruder.class);
                CraftTweakerAPI.registerClass(RiftCrafttweakerMillstone.class);
                CraftTweakerAPI.registerClass(RiftCrafttweakerMechanicalFilter.class);
            }
        }
    }
}
