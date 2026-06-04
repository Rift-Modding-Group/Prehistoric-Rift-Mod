package anightdazingzoroark.prift.client.rendering.entity;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureRegistry;
import anightdazingzoroark.prift.server.entity.serverModel.CreatureModel;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;

/**
 * This renderer is for rendering stuff on the client for the common creature model.
 * */
public class RiftCreatureRenderer extends GeoEntityRenderer<RiftCreature> {
    public RiftCreatureRenderer(RenderManager renderManager) {
        super(renderManager, new CreatureModel());
    }

    @Override
    public void render(GeoModel model, RiftCreature animatable, float partialTicks, float red, float green, float blue, float alpha) {
        //hide saddle stuff
        if (model.getBone("saddle").isPresent()) model.getBone("saddle").get().setHidden(true);
        if (model.getBone("headSaddle").isPresent()) model.getBone("headSaddle").get().setHidden(true);
        if (model.getBone("chest").isPresent()) model.getBone("chest").get().setHidden(true);
        if (model.getBone("hiddenBySaddle").isPresent()) model.getBone("hiddenBySaddle").get().setHidden(true);

        super.render(model, animatable, partialTicks, red, green, blue, alpha);
    }

    @Override
    protected float entityScale(RiftCreature animatable) {
        return animatable.scale();
    }
}
