package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.RiftSacModel;
import anightdazingzoroark.prift.server.entity.RiftSac;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoEntityRenderer;

public class RiftSacRenderer extends GeoEntityRenderer<RiftSac> {
    public RiftSacRenderer(RenderManager renderManager) {
        super(renderManager, new RiftSacModel());
        this.shadowSize = 0.5f;
    }

    @Override
    public void render(GeoModel model, RiftSac animatable, float partialTicks, float red, float green, float blue, float alpha) {
        float scale = animatable.getCreatureType().getEggScale();

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
        GlStateManager.popMatrix();
    }
}
