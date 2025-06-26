package anightdazingzoroark.prift.client.model.block;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityMechanicalFilter;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;

public class RiftMechanicalFilterModel extends AnimatedGeoModel<TileEntityMechanicalFilter> {
    @Override
    public ResourceLocation getModelLocation(TileEntityMechanicalFilter tileEntityMechanicalFilter) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/mechanical_filter.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TileEntityMechanicalFilter tileEntityMechanicalFilter) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/blocks/mechanical_filter.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TileEntityMechanicalFilter tileEntityMechanicalFilter) {
        return null;
    }
}
