package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.RiftEggModel;
import anightdazingzoroark.prift.client.model.RiftSacModel;
import anightdazingzoroark.prift.server.entity.RiftEgg;
import anightdazingzoroark.prift.server.entity.RiftSac;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

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
