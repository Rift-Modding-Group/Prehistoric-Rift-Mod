package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.RiftMortarShellModel;
import anightdazingzoroark.prift.server.entity.projectile.RiftMortarShell;
import anightdazingzoroark.riftlib.renderers.geo.GeoProjectileRenderer;
import net.minecraft.client.renderer.entity.RenderManager;

public class RiftMortarShellRenderer extends GeoProjectileRenderer<RiftMortarShell> {
    public RiftMortarShellRenderer(RenderManager renderManager) {
        super(renderManager, new RiftMortarShellModel());
    }
}
