package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.client.model.block.RiftMechanicalFilterModel;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityMechanicalFilter;
import anightdazingzoroark.riftlib.renderers.geo.GeoBlockRenderer;

public class MechanicalFilterRenderer extends GeoBlockRenderer<TileEntityMechanicalFilter> {
    public MechanicalFilterRenderer() {
        super(new RiftMechanicalFilterModel());
    }
}
