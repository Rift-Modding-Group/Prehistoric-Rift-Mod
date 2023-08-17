package anightdazingzoroark.rift.client.renderer.entity;

import anightdazingzoroark.rift.client.model.RiftCreatureModel;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class RiftCreatureRenderer extends GeoEntityRenderer<RiftCreature> {
    public RiftCreatureRenderer(RenderManager renderManager) {
        super(renderManager, new RiftCreatureModel());
        this.shadowSize = 1.0f;
    }

    @Override
    public void render(GeoModel model, RiftCreature animatable, float partialTicks, float red, float green, float blue, float alpha) {
        model.getBone("saddle").get().setHidden(true);
        model.getBone("headSaddle").get().setHidden(true);
        model.getBone("chest").get().setHidden(true);

        if (animatable.isChild()) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            super.render(model, animatable, partialTicks, red, green, blue, alpha);
            GlStateManager.popMatrix();
        }
        else {
            GlStateManager.pushMatrix();
            GlStateManager.scale(3.25F, 3.25F, 3.25F);
            super.render(model, animatable, partialTicks, red, green, blue, alpha);
            GlStateManager.popMatrix();
        }
    }
}
