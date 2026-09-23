package anightdazingzoroark.prift.client.rendering.entity;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.model.CreatureModel;
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
        boolean saddled = animatable.isSaddled();
        if (model.getAllBones().get("saddle") != null) model.getAllBones().get("saddle").setHidden(!saddled);
        if (model.getAllBones().get("headSaddle") != null) model.getAllBones().get("headSaddle").setHidden(!saddled);
        if (model.getAllBones().get("hiddenBySaddle") != null) model.getAllBones().get("hiddenBySaddle").setHidden(saddled);

        super.render(model, animatable, partialTicks, red, green, blue, alpha);
    }
}
