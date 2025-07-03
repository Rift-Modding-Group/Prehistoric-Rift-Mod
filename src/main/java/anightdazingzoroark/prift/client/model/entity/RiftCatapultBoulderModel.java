package anightdazingzoroark.prift.client.model.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.projectile.RiftCatapultBoulder;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;

public class RiftCatapultBoulderModel extends AnimatedGeoModel<RiftCatapultBoulder> {
    @Override
    public ResourceLocation getModelLocation(RiftCatapultBoulder riftCatapultBoulder) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/cannonball.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(RiftCatapultBoulder riftCatapultBoulder) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/entities/catapult_boulder.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(RiftCatapultBoulder riftCatapultBoulder) {
        return null;
    }
}
