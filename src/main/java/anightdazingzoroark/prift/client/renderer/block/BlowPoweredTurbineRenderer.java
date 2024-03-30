package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.client.model.block.RiftBlowPoweredTurbineModel;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityBlowPoweredTurbine;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityLeadPoweredCrank;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;
import software.bernie.geckolib3.resource.GeckoLibCache;

public class BlowPoweredTurbineRenderer extends GeoBlockRenderer<TileEntityBlowPoweredTurbine> {
    public BlowPoweredTurbineRenderer() {
        super(new RiftBlowPoweredTurbineModel());
    }

    @Override
    public void render(GeoModel model, TileEntityBlowPoweredTurbine animatable, float partialTicks, float red, float green, float blue, float alpha) {
        GeckoLibCache.getInstance().parser.setValue("rotation", animatable.getRotation());
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
    }
}
