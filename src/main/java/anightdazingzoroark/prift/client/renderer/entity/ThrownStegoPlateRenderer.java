package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.ThrownStegoPlateModel;
import anightdazingzoroark.prift.server.entity.projectile.ThrownStegoPlate;
import anightdazingzoroark.riftlib.renderers.geo.GeoProjectileRenderer;
import net.minecraft.client.renderer.entity.RenderManager;

public class ThrownStegoPlateRenderer extends GeoProjectileRenderer<ThrownStegoPlate> {
    public ThrownStegoPlateRenderer(RenderManager renderManager) {
        super(renderManager, new ThrownStegoPlateModel());
    }
}
