package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.RiftCannonballModel;
import anightdazingzoroark.prift.server.entity.projectile.RiftCannonball;
import anightdazingzoroark.riftlib.renderers.geo.GeoProjectileRenderer;
import net.minecraft.client.renderer.entity.RenderManager;

public class RiftCannonballRenderer extends GeoProjectileRenderer<RiftCannonball> {
    public RiftCannonballRenderer(RenderManager renderManager) {
        super(renderManager, new RiftCannonballModel());
    }
}
