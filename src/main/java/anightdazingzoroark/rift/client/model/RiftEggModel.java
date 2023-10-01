package anightdazingzoroark.rift.client.model;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.server.entity.RiftEgg;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class RiftEggModel extends AnimatedGeoModel<RiftEgg> {
    @Override
    public ResourceLocation getModelLocation(RiftEgg object) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/egg.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(RiftEgg object) {
        String name = object.getCreatureType().name().toLowerCase();
        return new ResourceLocation(RiftInitialize.MODID, "textures/entities/egg/"+name+"_egg.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(RiftEgg animatable) {
        return null;
    }
}
