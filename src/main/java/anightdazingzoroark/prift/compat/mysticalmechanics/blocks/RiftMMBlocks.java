package anightdazingzoroark.prift.compat.mysticalmechanics.blocks;

import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.blocks.RiftBlocks;
import com.codetaylor.mc.pyrotech.ModPyrotechConfig;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.ModuleTechBloomery;
import net.minecraft.block.Block;

public class RiftMMBlocks {
    public static Block LEAD_POWERED_CRANK;
    public static Block BLOW_POWERED_TURBINE;
    public static Block BLOW_POWERED_TURBINE_PART;
    public static Block HAND_CRANK;
    public static Block SEMI_MANUAL_EXTRACTOR;
    public static Block SEMI_MANUAL_EXTRACTOR_TOP;
    public static Block SEMI_MANUAL_PRESSER;
    public static Block SEMI_MANUAL_PRESSER_TOP;
    public static Block SEMI_MANUAL_EXTRUDER;
    public static Block SEMI_MANUAL_EXTRUDER_TOP;
    public static Block SEMI_MANUAL_HAMMERER;
    public static Block SEMI_MANUAL_HAMMERER_TOP;

    public static void registerMMBlocks() {
        LEAD_POWERED_CRANK = RiftBlocks.registerBlock(new BlockLeadPoweredCrank(), "lead_powered_crank", true, false);
        BLOW_POWERED_TURBINE = RiftBlocks.registerBlock(new BlockBlowPoweredTurbine(), "blow_powered_turbine", false);
        BLOW_POWERED_TURBINE_PART = RiftBlocks.registerBlock(new BlockBlowPoweredTurbinePart(), "blow_powered_turbine_part", false);
        HAND_CRANK = RiftBlocks.registerBlock(new BlockHandCrank(), "hand_crank", true, false);
        SEMI_MANUAL_EXTRACTOR = RiftBlocks.registerBlock(new BlockSemiManualExtractor(), "semi_manual_extractor" ,false);
        SEMI_MANUAL_EXTRACTOR_TOP = RiftBlocks.registerBlock(new BlockSemiManualExtractorTop(), "semi_manual_extractor_top", false);
        SEMI_MANUAL_PRESSER = RiftBlocks.registerBlock(new BlockSemiManualPresser(), "semi_manual_presser", false);
        SEMI_MANUAL_PRESSER_TOP = RiftBlocks.registerBlock(new BlockSemiManualPresserTop(), "semi_manual_presser_top", false);
        SEMI_MANUAL_EXTRUDER = RiftBlocks.registerBlock(new BlockSemiManualExtruder(), "semi_manual_extruder", false);
        SEMI_MANUAL_EXTRUDER_TOP = RiftBlocks.registerBlock(new BlockSemiManualExtruderTop(), "semi_manual_extruder_top", false);
        if (GeneralConfig.canUsePyrotech() && ModPyrotechConfig.MODULES.get(ModuleTechBloomery.MODULE_ID)) {
            SEMI_MANUAL_HAMMERER = RiftBlocks.registerBlock(new BlockSemiManualHammerer(), "semi_manual_hammerer", false);
            SEMI_MANUAL_HAMMERER_TOP = RiftBlocks.registerBlock(new BlockSemiManualHammererTop(), "semi_manual_hammerer_top", false);
        }
    }
}
