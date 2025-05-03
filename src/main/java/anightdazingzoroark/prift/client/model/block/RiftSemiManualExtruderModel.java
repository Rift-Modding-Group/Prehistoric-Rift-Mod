package anightdazingzoroark.prift.client.model.block;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualExtruder;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class RiftSemiManualExtruderModel extends AnimatedGeoModel<TileEntitySemiManualExtruder> {
    @Override
    public ResourceLocation getModelLocation(TileEntitySemiManualExtruder tileEntitySemiManualExtruder) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/semi_manual_extruder.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TileEntitySemiManualExtruder tileEntitySemiManualExtruder) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/blocks/semi_manual_extruder.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TileEntitySemiManualExtruder tileEntitySemiManualExtruder) {
        return new ResourceLocation(RiftInitialize.MODID, "animations/semi_manual_extractor.animation.json");
    }
}
