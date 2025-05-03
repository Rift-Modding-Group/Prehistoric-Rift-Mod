package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.model.entity.RiftCreatureModel;
import anightdazingzoroark.prift.client.model.entity.GlowingLayerModel;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.Sarcosuchus;
import anightdazingzoroark.prift.server.entity.creature.Saurophaganax;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

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
