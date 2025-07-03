package anightdazingzoroark.prift.client.model.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.projectile.DilophosaurusSpit;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;

public class DilophosaurusSpitModel extends AnimatedGeoModel<DilophosaurusSpit> {
    @Override
    public ResourceLocation getModelLocation(DilophosaurusSpit dilophosaurusSpit) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/dilophosaurus_spit.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(DilophosaurusSpit dilophosaurusSpit) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/entities/dilophosaurus_spit.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(DilophosaurusSpit dilophosaurusSpit) {
        return null;
    }
}
