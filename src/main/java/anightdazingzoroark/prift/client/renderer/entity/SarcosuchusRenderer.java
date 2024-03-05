package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.model.RiftCreatureModel;
import anightdazingzoroark.prift.server.entity.creature.Parasaurolophus;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.Sarcosuchus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class SarcosuchusRenderer extends GeoEntityRenderer<RiftCreature> {
    public SarcosuchusRenderer(RenderManager renderManager) {
        super(renderManager, new RiftCreatureModel());
    }

    @Override
    public boolean shouldRender(RiftCreature livingEntity, ICamera camera, double camX, double camY, double camZ) {
        Sarcosuchus sarcosuchus = (Sarcosuchus) livingEntity;
        return super.shouldRender(sarcosuchus, camera, camX, camY, camZ) || sarcosuchus.shouldRender(camera) || Minecraft.getMinecraft().player.isRidingOrBeingRiddenBy(sarcosuchus);
    }

    @Override
    public void render(GeoModel model, RiftCreature animatable, float partialTicks, float red, float green, float blue, float alpha) {
        float scale = RiftUtil.setModelScale(animatable, 0.3f, 1.5f);
        Sarcosuchus sarcosuchus = (Sarcosuchus) animatable;

        //hide saddle stuff
        model.getBone("saddle").get().setHidden(!sarcosuchus.isSaddled());
        model.getBone("headSaddle").get().setHidden(!sarcosuchus.isSaddled());
        model.getBone("chest").get().setHidden(true);
        model.getBone("hiddenBySaddle").get().setHidden(sarcosuchus.isSaddled());

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
        GlStateManager.popMatrix();
    }
}
