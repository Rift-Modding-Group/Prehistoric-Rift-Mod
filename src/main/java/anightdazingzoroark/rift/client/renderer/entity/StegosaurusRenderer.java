package anightdazingzoroark.rift.client.renderer.entity;

import anightdazingzoroark.rift.RiftUtil;
import anightdazingzoroark.rift.client.model.RiftCreatureModel;
import anightdazingzoroark.rift.server.entity.creature.RiftCreature;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.resource.GeckoLibCache;

public class StegosaurusRenderer extends GeoEntityRenderer<RiftCreature> {
    public StegosaurusRenderer(RenderManager renderManager) {
        super(renderManager, new RiftCreatureModel());
        this.shadowSize = 1.0f;
    }

    @Override
    public void render(GeoModel model, RiftCreature animatable, float partialTicks, float red, float green, float blue, float alpha) {
        float scale = RiftUtil.setModelScale(animatable, 0.3f, 2.125f);
//        float headYawRotation = RiftUtil.clamp(RiftUtil.getCreatureHeadYaw(animatable, partialTicks), -12.5f, 12.5f) * 0.017453292F;
//        float headPitchRotation = RiftUtil.clamp(RiftUtil.getCreatureHeadPitch(animatable, partialTicks), -12.5f, 12.5f) * 0.017453292F;
//
//        if (!animatable.isActing() && !animatable.hasTarget() && !animatable.isBeingRidden() && headPitchRotation != 0.017453292f) {
//            headPitchRotation = ((headPitchRotation > 0 ? (float)Math.floor(headPitchRotation) : (float)Math.ceil(headPitchRotation))/0.017453292F + (headPitchRotation > 0 ? -1f : 1f)) * 0.017453292F;
//        }

        //variables
        GeckoLibCache.getInstance().parser.setValue("use_plate_fling", RiftUtil.clamp(0.15D * (double)animatable.getRightClickUse(), 0D, 15D));

        //hide saddle stuff
        model.getBone("saddle").get().setHidden(!animatable.isSaddled());
        model.getBone("headSaddle").get().setHidden(!animatable.isSaddled());
        model.getBone("chest").get().setHidden(true);

        //change size and rotate neck
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
//        if (!animatable.isActing() && (animatable.hasTarget() || animatable.isBeingRidden())) model.getBone("neck").get().setRotationX(headPitchRotation);
//        model.getBone("neck").get().setRotationY(headYawRotation);
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
        GlStateManager.popMatrix();
    }
}
