package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.DilophosaurusSpitModel;
import anightdazingzoroark.prift.server.entity.projectile.DilophosaurusSpitAnimator;
import anightdazingzoroark.riftlib.renderers.geo.GeoItemRenderer;

public class DilophosaurusSpitRenderer extends GeoItemRenderer<DilophosaurusSpitAnimator> {
    public DilophosaurusSpitRenderer() {
        super(new DilophosaurusSpitModel());
    }
}
