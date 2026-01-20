package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.client.model.block.RiftSemiManualExtruderModel;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualExtruder;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoBlockRenderer;

public class SemiManualExtruderRenderer extends GeoBlockRenderer<TileEntitySemiManualExtruder> {
    public SemiManualExtruderRenderer() {
        super(new RiftSemiManualExtruderModel());
    }

    @Override
    public void render(GeoModel model, TileEntitySemiManualExtruder animatable, float partialTicks, float red, float green, float blue, float alpha) {
        model.getBone("spinAxle").get().setRotationY(animatable.getRotation());
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
    }
}
