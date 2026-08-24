package anightdazingzoroark.prift.client.rendering.entity;

import anightdazingzoroark.prift.client.model.ProjectileModel;
import anightdazingzoroark.prift.server.entity.projectile.RiftProjectile;
import anightdazingzoroark.riftlib.renderers.geo.GeoProjectileRenderer;
import net.minecraft.client.renderer.entity.RenderManager;

public class RiftProjectileRenderer extends GeoProjectileRenderer<RiftProjectile> {
    public RiftProjectileRenderer(RenderManager renderManager) {
        super(renderManager, new ProjectileModel());
    }
}
