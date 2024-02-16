package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.RiftCreatureModel;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class CoelacanthRenderer extends GeoEntityRenderer<RiftCreature> {
    public CoelacanthRenderer(RenderManager renderManager) {
        super(renderManager, new RiftCreatureModel());
    }
}
