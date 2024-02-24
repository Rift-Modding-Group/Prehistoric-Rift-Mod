package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.RiftCreatureModel;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class MegapiranhaRenderer extends GeoEntityRenderer<RiftCreature> {
    public MegapiranhaRenderer(RenderManager renderManager) {
        super(renderManager, new RiftCreatureModel());
    }
}
