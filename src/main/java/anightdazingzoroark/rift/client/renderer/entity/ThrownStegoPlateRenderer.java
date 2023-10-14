package anightdazingzoroark.rift.client.renderer.entity;

import anightdazingzoroark.rift.client.model.ThrownStegoPlateModel;
import anightdazingzoroark.rift.server.entity.projectile.ThrownStegoPlate;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;

public class ThrownStegoPlateRenderer extends GeoProjectilesRenderer<ThrownStegoPlate> {
    public ThrownStegoPlateRenderer(RenderManager renderManager) {
        super(renderManager, new ThrownStegoPlateModel());
    }

    @Override
    public void doRender(ThrownStegoPlate entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}
