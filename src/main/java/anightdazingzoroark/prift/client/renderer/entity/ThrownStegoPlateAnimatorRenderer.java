package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.ThrownStegoPlateModel;
import anightdazingzoroark.prift.server.entity.projectile.ThrownStegoPlateAnimator;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class ThrownStegoPlateAnimatorRenderer extends GeoItemRenderer<ThrownStegoPlateAnimator> {
    public ThrownStegoPlateAnimatorRenderer(int variant) {
        super(new ThrownStegoPlateModel(variant));
    }
}
