package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.model.RiftCreatureModel;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.Tyrannosaurus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.resource.GeckoLibCache;

public class TyrannosaurusRenderer extends GeoEntityRenderer<RiftCreature> {
    public TyrannosaurusRenderer(RenderManager renderManager) {
        super(renderManager, new RiftCreatureModel());
        this.shadowSize = 1.0f;
    }

    @Override
    public boolean shouldRender(RiftCreature livingEntity, ICamera camera, double camX, double camY, double camZ) {
        Tyrannosaurus tyrannosaurus = (Tyrannosaurus) livingEntity;
        return super.shouldRender(tyrannosaurus, camera, camX, camY, camZ) || tyrannosaurus.shouldRender(camera) || Minecraft.getMinecraft().player.isRidingOrBeingRiddenBy(tyrannosaurus);
    }

    @Override
    public void render(GeoModel model, RiftCreature animatable, float partialTicks, float red, float green, float blue, float alpha) {
        float scale = RiftUtil.setModelScale(animatable, 0.5f, 3.25f);

        //variables
        GeckoLibCache.getInstance().parser.setValue("use_roar", animatable.getRightClickUse());
//        GeckoLibCache.getInstance().parser.setValue("look_pitch", (double) animatable.rotationPitch);
//        GeckoLibCache.getInstance().parser.setValue("look_yaw", (double) animatable.rotationYaw);

        //hide saddle stuff
        model.getBone("saddle").get().setHidden(!animatable.isSaddled());
        model.getBone("headSaddle").get().setHidden(!animatable.isSaddled());
        model.getBone("chest").get().setHidden(true);

        //change size and rotate neck
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
        GlStateManager.popMatrix();
    }
}
