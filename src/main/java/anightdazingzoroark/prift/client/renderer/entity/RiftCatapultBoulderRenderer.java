package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.RiftCatapultBoulderModel;
import anightdazingzoroark.prift.server.entity.projectile.RiftCatapultBoulder;
import anightdazingzoroark.riftlib.renderers.geo.GeoProjectileRenderer;
import net.minecraft.client.renderer.entity.RenderManager;

public class RiftCatapultBoulderRenderer extends GeoProjectileRenderer<RiftCatapultBoulder> {
    public RiftCatapultBoulderRenderer(RenderManager renderManager) {
        super(renderManager, new RiftCatapultBoulderModel());
    }
}
