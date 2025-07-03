package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.DilophosaurusSpitModel;
import anightdazingzoroark.prift.server.entity.projectile.DilophosaurusSpit;
import anightdazingzoroark.riftlib.renderers.geo.GeoProjectileRenderer;
import net.minecraft.client.renderer.entity.RenderManager;

public class DilophosaurusSpitRenderer extends GeoProjectileRenderer<DilophosaurusSpit> {
    public DilophosaurusSpitRenderer(RenderManager renderManager) {
        super(renderManager, new DilophosaurusSpitModel());
    }
}
