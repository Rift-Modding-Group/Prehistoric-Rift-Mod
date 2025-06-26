package anightdazingzoroark.prift.client.model.block;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualPresser;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;

public class RiftSemiManualPresserModel extends AnimatedGeoModel<TileEntitySemiManualPresser> {
    @Override
    public ResourceLocation getModelLocation(TileEntitySemiManualPresser tileEntitySemiManualPresser) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/semi_manual_presser.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TileEntitySemiManualPresser tileEntitySemiManualPresser) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/blocks/semi_manual_presser.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TileEntitySemiManualPresser tileEntitySemiManualPresser) {
        return new ResourceLocation(RiftInitialize.MODID, "animations/semi_manual_extractor.animation.json");
    }
}
