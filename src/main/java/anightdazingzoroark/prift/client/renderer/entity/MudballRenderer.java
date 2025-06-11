package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.MudballModel;
import anightdazingzoroark.prift.server.entity.projectile.MudballAnimator;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class MudballRenderer extends GeoItemRenderer<MudballAnimator> {
    public MudballRenderer() {
        super(new MudballModel());
    }
}
