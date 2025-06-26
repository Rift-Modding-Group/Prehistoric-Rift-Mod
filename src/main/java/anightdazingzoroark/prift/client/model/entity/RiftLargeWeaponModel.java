package anightdazingzoroark.prift.client.model.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftCatapult;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;

public class RiftLargeWeaponModel extends AnimatedGeoModel<RiftLargeWeapon> {
    @Override
    public ResourceLocation getModelLocation(RiftLargeWeapon object) {
        String name = object.weaponType.name().toLowerCase();
        return new ResourceLocation(RiftInitialize.MODID, "geo/"+name+".model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(RiftLargeWeapon object) {
        String name = object.weaponType.name().toLowerCase();
        return new ResourceLocation(RiftInitialize.MODID, "textures/entities/"+name+".png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(RiftLargeWeapon object) {
        if (object instanceof RiftCatapult) return new ResourceLocation(RiftInitialize.MODID, "animations/catapult.animation.json");
        return null;
    }
}
