package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.client.model.block.RiftSemiManualExtractorModel;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualExtractor;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class SemiManualExtractorRenderer extends GeoBlockRenderer<TileEntitySemiManualExtractor> {
    public SemiManualExtractorRenderer() {
        super(new RiftSemiManualExtractorModel());
    }
}
