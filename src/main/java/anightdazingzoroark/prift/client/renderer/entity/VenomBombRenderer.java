package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.VenomBombModel;
import anightdazingzoroark.prift.server.entity.projectile.VenomBombAnimator;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class VenomBombRenderer extends GeoItemRenderer<VenomBombAnimator> {
    public VenomBombRenderer() {
        super(new VenomBombModel());
    }
}
