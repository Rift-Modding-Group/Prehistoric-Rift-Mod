package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.client.model.block.RiftSemiManualPresserModel;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualPresser;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class SemiManualPresserRenderer extends GeoBlockRenderer<TileEntitySemiManualPresser> {
    public SemiManualPresserRenderer() {
        super(new RiftSemiManualPresserModel());
    }
}
