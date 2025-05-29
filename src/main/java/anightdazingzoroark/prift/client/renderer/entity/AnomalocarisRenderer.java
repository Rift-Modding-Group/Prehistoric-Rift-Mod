package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.Anomalocaris;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;

public class AnomalocarisRenderer extends RiftCreatureRenderer {
    public AnomalocarisRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(GeoModel model, RiftCreature animatable, float partialTicks, float red, float green, float blue, float alpha) {
        Anomalocaris anomalocaris = (Anomalocaris) animatable;
        float scale = RiftUtil.setModelScale(anomalocaris, 1f, 2f);
        float translucency = anomalocaris.isCloaked() ? 0.2f : 1f;

        //hide saddle stuff
        model.getBone("saddle").get().setHidden(!anomalocaris.isSaddled());
        model.getBone("leftAppendageSaddle").get().setHidden(!anomalocaris.isSaddled());
        model.getBone("rightAppendageSaddle").get().setHidden(!anomalocaris.isSaddled());
        model.getBone("chest").get().setHidden(true);

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, translucency);
        super.render(model, anomalocaris, partialTicks, red, green, blue, translucency);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
