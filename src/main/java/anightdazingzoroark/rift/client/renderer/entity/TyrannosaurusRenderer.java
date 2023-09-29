package anightdazingzoroark.rift.client.renderer.entity;

import anightdazingzoroark.rift.RiftUtil;
import anightdazingzoroark.rift.client.model.RiftCreatureModel;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class TyrannosaurusRenderer extends GeoEntityRenderer<RiftCreature> {
    public TyrannosaurusRenderer(RenderManager renderManager) {
        super(renderManager, new RiftCreatureModel());
        this.shadowSize = 1.0f;
    }

    @Override
    public void render(GeoModel model, RiftCreature animatable, float partialTicks, float red, float green, float blue, float alpha) {
        float scale = ((3.25f - 0.5f) / (24000f)) * (animatable.getAgeInTicks() - 24000f) + 3.25f;
        float headYawRotation = RiftUtil.clamp(RiftUtil.getCreatureHeadYaw(animatable, partialTicks), -12.5f, 12.5f) * 0.017453292F;
        float headPitchRotation = RiftUtil.clamp(RiftUtil.getCreatureHeadPitch(animatable, partialTicks), -12.5f, 12.5f) * 0.017453292F;

        if (!animatable.hasTarget() && !animatable.isBeingRidden() && headPitchRotation != 0f) {
            headPitchRotation = ((headPitchRotation > 0 ? (float)Math.floor(headPitchRotation) : (float)Math.ceil(headPitchRotation))/0.017453292F + (headPitchRotation > 0 ? -1f : 1f)) * 0.017453292F;
        }

        //hide saddle stuff
        model.getBone("saddle").get().setHidden(!animatable.isSaddled());
        model.getBone("headSaddle").get().setHidden(!animatable.isSaddled());
        model.getBone("chest").get().setHidden(true);

        //change size and rotate neck
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        model.getBone("neck").get().setRotationX(headPitchRotation);
        model.getBone("neck").get().setRotationY(headYawRotation);
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
        GlStateManager.popMatrix();
    }
}
