package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.client.model.block.RiftSemiManualExtruderModel;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualExtruder;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoBlockRenderer;

public class SemiManualExtruderRenderer extends GeoBlockRenderer<TileEntitySemiManualExtruder> {
    public SemiManualExtruderRenderer() {
        super(new RiftSemiManualExtruderModel());
    }
}
