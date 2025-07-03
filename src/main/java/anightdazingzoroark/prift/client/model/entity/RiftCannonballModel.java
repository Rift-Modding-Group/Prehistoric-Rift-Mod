package anightdazingzoroark.prift.client.model.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.projectile.RiftCannonball;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;

public class RiftCannonballModel extends AnimatedGeoModel<RiftCannonball> {
    @Override
    public ResourceLocation getModelLocation(RiftCannonball riftCannonball) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/cannonball.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(RiftCannonball riftCannonball) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/entities/cannonball.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(RiftCannonball riftCannonball) {
        return null;
    }
}
