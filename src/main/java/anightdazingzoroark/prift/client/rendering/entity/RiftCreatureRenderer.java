package anightdazingzoroark.prift.client.rendering.entity;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.serverModel.CreatureModel;
import anightdazingzoroark.riftlib.geo.GeoModel;
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
        if (model.allBones.get("saddle") != null) model.allBones.get("saddle").setHidden(true);
        if (model.allBones.get("headSaddle") != null) model.allBones.get("headSaddle").setHidden(true);
        if (model.allBones.get("hiddenBySaddle") != null) model.allBones.get("hiddenBySaddle").setHidden(true);

        super.render(model, animatable, partialTicks, red, green, blue, alpha);
    }

    @Override
    protected float entityScale(RiftCreature animatable) {
        return animatable.scale();
    }
}
