package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.DilophosaurusSpitModel;
import anightdazingzoroark.prift.server.entity.projectile.DilophosaurusSpitAnimator;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class DilophosaurusSpitRenderer extends GeoItemRenderer<DilophosaurusSpitAnimator> {
    public DilophosaurusSpitRenderer() {
        super(new DilophosaurusSpitModel());
    }
}
