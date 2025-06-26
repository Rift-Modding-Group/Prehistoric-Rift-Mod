package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.RiftEmbryoModel;
import anightdazingzoroark.prift.server.entity.other.RiftEmbryo;
import net.minecraft.client.renderer.entity.RenderManager;
import anightdazingzoroark.riftlib.renderers.geo.GeoEntityRenderer;

public class RiftEmbryoRenderer extends GeoEntityRenderer<RiftEmbryo> {
    public RiftEmbryoRenderer(RenderManager renderManager) {
        super(renderManager, new RiftEmbryoModel());
    }
}
