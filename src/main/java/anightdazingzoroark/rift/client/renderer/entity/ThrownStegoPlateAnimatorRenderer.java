package anightdazingzoroark.rift.client.renderer.entity;

import anightdazingzoroark.rift.client.model.ThrownStegoPlateModel;
import anightdazingzoroark.rift.server.entity.projectile.ThrownStegoPlateAnimator;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class ThrownStegoPlateAnimatorRenderer extends GeoItemRenderer<ThrownStegoPlateAnimator> {
    public ThrownStegoPlateAnimatorRenderer(int variant) {
        super(new ThrownStegoPlateModel(variant));
    }
}
