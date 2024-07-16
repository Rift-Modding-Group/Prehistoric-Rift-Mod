package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.RiftCreatureModel;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class PalaeocastorRenderer extends GeoEntityRenderer<RiftCreature> {
    public PalaeocastorRenderer(RenderManager renderManager) {
        super(renderManager, new RiftCreatureModel());
    }
}
