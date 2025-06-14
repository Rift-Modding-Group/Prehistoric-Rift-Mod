package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.GlowingLayerModel;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.renderer.entity.RenderManager;

public class RiftCreatureGlowingRenderer extends RiftCreatureRenderer {
    public RiftCreatureGlowingRenderer(RenderManager renderManager) {
        super(renderManager);
        this.addLayer(new GlowingLayerModel<RiftCreature>(this, this.getGeoModelProvider()::getTextureLocation, this.getGeoModelProvider()::getModelLocation));
    }
}
