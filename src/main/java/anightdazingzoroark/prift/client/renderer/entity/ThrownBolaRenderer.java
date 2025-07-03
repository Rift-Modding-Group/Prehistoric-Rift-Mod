package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.ThrownBolaModel;
import anightdazingzoroark.prift.server.entity.projectile.ThrownBola;
import anightdazingzoroark.riftlib.renderers.geo.GeoProjectileRenderer;
import net.minecraft.client.renderer.entity.RenderManager;

public class ThrownBolaRenderer extends GeoProjectileRenderer<ThrownBola> {
    public ThrownBolaRenderer(RenderManager renderManager) {
        super(renderManager, new ThrownBolaModel());
    }
}
