package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.RiftLargeWeaponModel;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class RiftLargeWeaponRenderer extends GeoEntityRenderer<RiftLargeWeapon> {
    public RiftLargeWeaponRenderer(RenderManager renderManager) {
        super(renderManager, new RiftLargeWeaponModel());
    }
}
