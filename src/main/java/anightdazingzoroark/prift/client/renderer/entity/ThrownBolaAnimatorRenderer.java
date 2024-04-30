package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.ThrownBolaModel;
import anightdazingzoroark.prift.server.entity.projectile.ThrownBolaAnimator;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class ThrownBolaAnimatorRenderer extends GeoItemRenderer<ThrownBolaAnimator> {
    public ThrownBolaAnimatorRenderer() {
        super(new ThrownBolaModel());
    }
}
