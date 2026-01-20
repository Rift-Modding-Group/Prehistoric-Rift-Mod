package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.client.model.block.RiftBlowPoweredTurbineModel;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityBlowPoweredTurbine;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoBlockRenderer;
import anightdazingzoroark.riftlib.resource.RiftLibCache;

public class BlowPoweredTurbineRenderer extends GeoBlockRenderer<TileEntityBlowPoweredTurbine> {
    public BlowPoweredTurbineRenderer() {
        super(new RiftBlowPoweredTurbineModel());
    }

    @Override
    public void render(GeoModel model, TileEntityBlowPoweredTurbine animatable, float partialTicks, float red, float green, float blue, float alpha) {
        RiftLibCache.getInstance().parser.setValue("rotation", animatable.getRotation());
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
    }
}
