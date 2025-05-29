package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.client.model.entity.GlowingLayerModel;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;

public class SaurophaganaxRenderer extends RiftCreatureRenderer {
    public SaurophaganaxRenderer(RenderManager renderManager) {
        super(renderManager);
        this.addLayer(new GlowingLayerModel<RiftCreature>(this, this.getGeoModelProvider()::getTextureLocation, this.getGeoModelProvider()::getModelLocation));
    }

    @Override
    public void render(GeoModel model, RiftCreature creature, float partialTicks, float red, float green, float blue, float alpha) {
        float scale = RiftUtil.setModelScale(creature, 0.2f, 2f);

        //hide saddle stuff
        model.getBone("saddle").get().setHidden(!creature.isSaddled());
        model.getBone("headSaddle").get().setHidden(!creature.isSaddled());
        model.getBone("chest").get().setHidden(true);
        model.getBone("hiddenBySaddle").get().setHidden(creature.isSaddled());

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        super.render(model, creature, partialTicks, red, green, blue, alpha);
        GlStateManager.popMatrix();
    }
}
