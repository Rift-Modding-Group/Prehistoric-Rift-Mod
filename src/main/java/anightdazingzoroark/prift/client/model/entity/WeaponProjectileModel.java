package anightdazingzoroark.prift.client.model.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.projectile.WeaponProjectileAnimator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class WeaponProjectileModel extends AnimatedGeoModel<WeaponProjectileAnimator> {
    private RiftLargeWeaponType weaponType;

    public WeaponProjectileModel(RiftLargeWeaponType weaponType) {
        this.weaponType = weaponType;
    }
    @Override
    public ResourceLocation getModelLocation(WeaponProjectileAnimator object) {
        switch (this.weaponType) {
            case CANNON:
            case CATAPULT:
                return new ResourceLocation(RiftInitialize.MODID, "geo/cannonball.model.json");
            case MORTAR:
                return new ResourceLocation(RiftInitialize.MODID, "geo/mortar_shell.model.json");
        }
        return new ResourceLocation(RiftInitialize.MODID, "geo/cannonball.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(WeaponProjectileAnimator object) {
        switch (this.weaponType) {
            case CANNON:
                return new ResourceLocation(RiftInitialize.MODID, "textures/entities/cannonball.png");
            case MORTAR:
                return new ResourceLocation(RiftInitialize.MODID, "textures/entities/mortar_shell.png");
            case CATAPULT:
                return new ResourceLocation(RiftInitialize.MODID, "textures/entities/catapult_boulder.png");
        }
        return new ResourceLocation(RiftInitialize.MODID, "textures/entities/cannonball.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(WeaponProjectileAnimator animatable) {
        return null;
    }
}
