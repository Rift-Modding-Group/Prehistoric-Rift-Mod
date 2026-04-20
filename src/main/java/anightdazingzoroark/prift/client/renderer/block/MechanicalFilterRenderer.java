package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.client.model.block.RiftMechanicalFilterModel;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityMechanicalFilter;
import anightdazingzoroark.riftlib.renderers.geo.GeoTileEntityRenderer;

public class MechanicalFilterRenderer extends GeoTileEntityRenderer<TileEntityMechanicalFilter> {
    public MechanicalFilterRenderer() {
        super(new RiftMechanicalFilterModel());
    }
}
