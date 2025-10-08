package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.RiftCreatureProjectileModel;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.projectile.RiftCreatureProjectileEntity;
import anightdazingzoroark.riftlib.geo.render.built.GeoModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoProjectileRenderer;
import net.minecraft.client.renderer.entity.RenderManager;

public class RiftCreatureProjectileRenderer extends GeoProjectileRenderer<RiftCreatureProjectileEntity> {
    public RiftCreatureProjectileRenderer(RenderManager renderManager) {
        super(renderManager, new RiftCreatureProjectileModel());
    }

    @Override
    public void render(GeoModel model, RiftCreatureProjectileEntity animatable, float partialTicks, float red, float green, float blue, float alpha) {
        //if projectile is meant to have no model, just hide it usin this
        if (animatable.hasNoModel()) return;

        super.render(model, animatable, partialTicks, red, green, blue, alpha);
    }
}
