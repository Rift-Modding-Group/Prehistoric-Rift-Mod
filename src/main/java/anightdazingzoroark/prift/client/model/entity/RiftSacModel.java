package anightdazingzoroark.prift.client.model.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.RiftSac;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;

public class RiftSacModel extends AnimatedGeoModel<RiftSac> {
    @Override
    public ResourceLocation getModelLocation(RiftSac object) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/sac.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(RiftSac object) {
        String name = object.getCreatureType().name().toLowerCase();
        return new ResourceLocation(RiftInitialize.MODID, "textures/entities/sac/"+name+"_sac.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(RiftSac object) {
        return null;
    }
}
