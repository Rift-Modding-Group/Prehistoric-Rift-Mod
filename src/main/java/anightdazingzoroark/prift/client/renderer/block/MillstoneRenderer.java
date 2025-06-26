package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.client.model.block.RiftMillstoneModel;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityMillstone;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoBlockRenderer;

public class MillstoneRenderer extends GeoBlockRenderer<TileEntityMillstone> {
    public MillstoneRenderer() {
        super(new RiftMillstoneModel());
    }
}
