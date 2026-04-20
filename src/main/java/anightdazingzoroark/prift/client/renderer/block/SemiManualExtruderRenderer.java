package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.client.model.block.RiftSemiManualExtruderModel;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualExtruder;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoTileEntityRenderer;

public class SemiManualExtruderRenderer extends GeoTileEntityRenderer<TileEntitySemiManualExtruder> {
    public SemiManualExtruderRenderer() {
        super(new RiftSemiManualExtruderModel());
    }
}
