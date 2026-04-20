package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.RiftCreatureNewModel;
import anightdazingzoroark.prift.server.entity.creaturenew.RiftCreatureNew;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;

public class RiftCreatureNewRenderer extends GeoEntityRenderer<RiftCreatureNew> {
    public RiftCreatureNewRenderer(RenderManager renderManager) {
        super(renderManager, new RiftCreatureNewModel());
    }

    @Override
    public void render(GeoModel model, RiftCreatureNew animatable, float partialTicks, float red, float green, float blue, float alpha) {
        //other conditions where the creature must not render
        //if (animatable.isBurrowing()) return;

        //hide saddle stuff
        if (model.getBone("saddle").isPresent()) model.getBone("saddle").get().setHidden(true);
        if (model.getBone("headSaddle").isPresent()) model.getBone("headSaddle").get().setHidden(true);
        if (model.getBone("chest").isPresent()) model.getBone("chest").get().setHidden(true);
        if (model.getBone("hiddenBySaddle").isPresent()) model.getBone("hiddenBySaddle").get().setHidden(true);

        super.render(model, animatable, partialTicks, red, green, blue, alpha);
    }

    @Override
    protected float entityScale(RiftCreatureNew entityLiving) {
        return entityLiving.scale();
    }
}
