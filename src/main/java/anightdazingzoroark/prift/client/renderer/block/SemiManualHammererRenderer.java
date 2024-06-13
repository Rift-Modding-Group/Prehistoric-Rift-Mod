package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.client.model.block.RiftSemiManualHammererModel;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualHammerer;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class SemiManualHammererRenderer extends GeoBlockRenderer<TileEntitySemiManualHammerer> {
    public SemiManualHammererRenderer() {
        super(new RiftSemiManualHammererModel());
    }
}
