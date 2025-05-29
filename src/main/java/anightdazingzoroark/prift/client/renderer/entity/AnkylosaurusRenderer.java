package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;

public class AnkylosaurusRenderer extends RiftCreatureRenderer {
    public AnkylosaurusRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(GeoModel model, RiftCreature animatable, float partialTicks, float red, float green, float blue, float alpha) {
        float scale = RiftUtil.setModelScale(animatable, 0.5f, 2.125f);

        //hide saddle stuff
        model.getBone("saddle").get().setHidden(!animatable.isSaddled());
        model.getBone("spikeSaddle").get().setHidden(!animatable.isSaddled());
        model.getBone("spike36").get().setHidden(animatable.isSaddled());
        model.getBone("spike37").get().setHidden(animatable.isSaddled());
        model.getBone("spike38").get().setHidden(animatable.isSaddled());
        model.getBone("spike39").get().setHidden(animatable.isSaddled());
        model.getBone("spike42").get().setHidden(animatable.isSaddled());
        model.getBone("spike43").get().setHidden(animatable.isSaddled());
        model.getBone("chest").get().setHidden(true);

        //change size and rotate neck
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
        GlStateManager.popMatrix();
    }
}
