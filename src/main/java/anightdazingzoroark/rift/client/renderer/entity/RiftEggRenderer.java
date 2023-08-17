package anightdazingzoroark.rift.client.renderer.entity;

import anightdazingzoroark.rift.client.model.RiftEggModel;
import anightdazingzoroark.rift.server.entity.RiftEgg;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class RiftEggRenderer extends GeoEntityRenderer<RiftEgg> {
    public RiftEggRenderer(RenderManager renderManager) {
        super(renderManager, new RiftEggModel());
        this.shadowSize = 0.5f;
    }
}
