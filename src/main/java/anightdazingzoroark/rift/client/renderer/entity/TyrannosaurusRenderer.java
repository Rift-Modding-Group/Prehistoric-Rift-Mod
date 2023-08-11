package anightdazingzoroark.rift.client.renderer.entity;

import anightdazingzoroark.rift.client.model.TyrannosaurusModel;
import anightdazingzoroark.rift.server.entity.creature.Tyrannosaurus;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class TyrannosaurusRenderer extends GeoEntityRenderer<Tyrannosaurus> {
    public TyrannosaurusRenderer(RenderManager renderManager) {
        super(renderManager, new TyrannosaurusModel());
        this.shadowSize = 1.0f;
    }

    @Override
    public void render(GeoModel model, Tyrannosaurus animatable, float partialTicks, float red, float green, float blue, float alpha) {
        model.getBone("saddle").get().setHidden(true);
        model.getBone("headSaddle").get().setHidden(true);
        model.getBone("chest").get().setHidden(true);

        GlStateManager.pushMatrix();
        GlStateManager.scale(3.25F, 3.25F, 3.25F);
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
        GlStateManager.popMatrix();
    }
}
