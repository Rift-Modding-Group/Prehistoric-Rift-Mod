package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.RiftEggModel;
import anightdazingzoroark.prift.server.entity.RiftEgg;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class RiftEggRenderer extends GeoEntityRenderer<RiftEgg> {
    public RiftEggRenderer(RenderManager renderManager) {
        super(renderManager, new RiftEggModel());
        this.shadowSize = 0.5f;
    }

    @Override
    public void render(GeoModel model, RiftEgg animatable, float partialTicks, float red, float green, float blue, float alpha) {
        float scale = animatable.getCreatureType().getEggScale();

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
        GlStateManager.popMatrix();
    }
}
