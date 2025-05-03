package anightdazingzoroark.prift.client.renderer.entity;

import anightdazingzoroark.prift.client.model.entity.WeaponProjectileModel;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.projectile.WeaponProjectileAnimator;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class WeaponProjectileAnimatorRenderer extends GeoItemRenderer<WeaponProjectileAnimator> {
    public WeaponProjectileAnimatorRenderer(RiftLargeWeaponType weaponType) {
        super(new WeaponProjectileModel(weaponType));
    }
}
