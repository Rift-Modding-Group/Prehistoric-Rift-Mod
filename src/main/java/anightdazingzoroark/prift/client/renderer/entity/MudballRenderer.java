package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.MudballModel;
import anightdazingzoroark.prift.server.entity.projectile.Mudball;
import anightdazingzoroark.riftlib.renderers.geo.GeoProjectileRenderer;
import net.minecraft.client.renderer.entity.RenderManager;

public class MudballRenderer extends GeoProjectileRenderer<Mudball> {
    public MudballRenderer(RenderManager renderManager) {
        super(renderManager, new MudballModel());
    }
}
