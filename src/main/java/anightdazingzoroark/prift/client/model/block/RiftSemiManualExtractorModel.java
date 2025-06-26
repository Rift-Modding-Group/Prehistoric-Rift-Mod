package anightdazingzoroark.prift.client.model.block;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualExtractor;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;

public class RiftSemiManualExtractorModel extends AnimatedGeoModel<TileEntitySemiManualExtractor> {
    @Override
    public ResourceLocation getModelLocation(TileEntitySemiManualExtractor tileEntitySemiManualExtractor) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/semi_manual_extractor.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TileEntitySemiManualExtractor tileEntitySemiManualExtractor) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/blocks/semi_manual_extractor.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TileEntitySemiManualExtractor tileEntitySemiManualExtractor) {
        return new ResourceLocation(RiftInitialize.MODID, "animations/semi_manual_extractor.animation.json");
    }
}
