package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;

public class DimetrodonRenderer extends RiftCreatureRenderer {
    public DimetrodonRenderer(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 1f;
    }

    @Override
    public void render(GeoModel model, RiftCreature animatable, float partialTicks, float red, float green, float blue, float alpha) {
        float scale = RiftUtil.setModelScale(animatable, 0.4f, 1.25f);

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
        GlStateManager.popMatrix();
    }
}
