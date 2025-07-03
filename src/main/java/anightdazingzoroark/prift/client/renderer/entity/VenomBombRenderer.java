package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.VenomBombModel;
import anightdazingzoroark.prift.server.entity.projectile.VenomBomb;
import anightdazingzoroark.riftlib.renderers.geo.GeoProjectileRenderer;
import net.minecraft.client.renderer.entity.RenderManager;

public class VenomBombRenderer extends GeoProjectileRenderer<VenomBomb> {
    public VenomBombRenderer(RenderManager renderManager) {
        super(renderManager, new VenomBombModel());
    }
}
