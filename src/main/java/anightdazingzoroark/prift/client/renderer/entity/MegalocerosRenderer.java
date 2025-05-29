package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;

public class MegalocerosRenderer extends RiftCreatureRenderer {
    public MegalocerosRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(GeoModel model, RiftCreature animatable, float partialTicks, float red, float green, float blue, float alpha) {
        float scale = RiftUtil.setModelScale(animatable, 0.3f, 1.125f);

        model.getBone("saddle").get().setHidden(!animatable.isSaddled());
        model.getBone("headSaddle").get().setHidden(!animatable.isSaddled());
        model.getBone("antlers").get().setHidden(animatable.isBaby());
        model.getBone("chest").get().setHidden(true);

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
        GlStateManager.popMatrix();
    }
}
