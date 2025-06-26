package anightdazingzoroark.prift.client.model.block;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualHammerer;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;

public class RiftSemiManualHammererModel extends AnimatedGeoModel<TileEntitySemiManualHammerer> {
    @Override
    public ResourceLocation getModelLocation(TileEntitySemiManualHammerer tileEntitySemiManualHammerer) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/semi_manual_hammerer.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TileEntitySemiManualHammerer tileEntitySemiManualHammerer) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/blocks/semi_manual_hammerer.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TileEntitySemiManualHammerer tileEntitySemiManualHammerer) {
        return new ResourceLocation(RiftInitialize.MODID, "animations/semi_manual_extractor.animation.json");
    }
}
