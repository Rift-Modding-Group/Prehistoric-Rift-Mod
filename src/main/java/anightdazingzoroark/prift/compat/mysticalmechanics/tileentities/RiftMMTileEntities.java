package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.RiftInitialize;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class RiftMMTileEntities {
    public static void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityLeadPoweredCrank.class, new ResourceLocation(RiftInitialize.MODID, "lead_powered_crank"));
        GameRegistry.registerTileEntity(TileEntityBlowPoweredTurbine.class, new ResourceLocation(RiftInitialize.MODID, "blow_powered_turbine"));
        GameRegistry.registerTileEntity(TileEntityBlowPoweredTurbinePart.class, new ResourceLocation(RiftInitialize.MODID, "blow_powered_turbine_part"));
        GameRegistry.registerTileEntity(TileEntityHandCrank.class, new ResourceLocation(RiftInitialize.MODID, "hand_crank"));
        GameRegistry.registerTileEntity(TileEntitySemiManualExtractor.class, new ResourceLocation(RiftInitialize.MODID, "semi_manual_extractor"));
        GameRegistry.registerTileEntity(TileEntitySemiManualExtractorTop.class, new ResourceLocation(RiftInitialize.MODID, "semi_manual_extractor_top"));
        GameRegistry.registerTileEntity(TileEntitySemiManualPresser.class, new ResourceLocation(RiftInitialize.MODID, "semi_manual_presser"));
        GameRegistry.registerTileEntity(TileEntitySemiManualPresserTop.class, new ResourceLocation(RiftInitialize.MODID, "semi_manual_presser_top"));
        GameRegistry.registerTileEntity(TileEntitySemiManualExtruder.class, new ResourceLocation(RiftInitialize.MODID, "semi_manual_extruder"));
        GameRegistry.registerTileEntity(TileEntitySemiManualExtruderTop.class, new ResourceLocation(RiftInitialize.MODID, "semi_manual_extruder_top"));
        GameRegistry.registerTileEntity(TileEntitySemiManualHammerer.class, new ResourceLocation(RiftInitialize.MODID, "semi_manual_hammerer"));
        GameRegistry.registerTileEntity(TileEntitySemiManualHammererTop.class, new ResourceLocation(RiftInitialize.MODID, "semi_manual_hammerer_top"));
    }
}
