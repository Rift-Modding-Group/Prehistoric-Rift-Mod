package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.client.model.block.RiftBlowPoweredTurbineModel;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityBlowPoweredTurbine;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class BlowPoweredTurbineRenderer extends GeoBlockRenderer<TileEntityBlowPoweredTurbine> {
    public BlowPoweredTurbineRenderer() {
        super(new RiftBlowPoweredTurbineModel());
    }
}
