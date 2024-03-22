package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.client.model.block.RiftLeadPoweredCrankModel;
import anightdazingzoroark.prift.compat.bwm.tileentities.TileEntityLeadPoweredCrank;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class LeadPoweredCrankRenderer extends GeoBlockRenderer<TileEntityLeadPoweredCrank> {
    public LeadPoweredCrankRenderer() {
        super(new RiftLeadPoweredCrankModel());
    }
}
