package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.RiftLargeWeaponModel;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class RiftLargeWeaponRenderer extends GeoEntityRenderer<RiftLargeWeapon> {
    public RiftLargeWeaponRenderer(RenderManager renderManager) {
        super(renderManager, new RiftLargeWeaponModel());
    }
}
