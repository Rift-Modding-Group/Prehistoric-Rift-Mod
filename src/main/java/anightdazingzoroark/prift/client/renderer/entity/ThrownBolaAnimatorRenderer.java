package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.ThrownBolaModel;
import anightdazingzoroark.prift.server.entity.projectile.ThrownBolaAnimator;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoItemRenderer;

public class ThrownBolaAnimatorRenderer extends GeoItemRenderer<ThrownBolaAnimator> {
    public ThrownBolaAnimatorRenderer() {
        super(new ThrownBolaModel());
    }
}
