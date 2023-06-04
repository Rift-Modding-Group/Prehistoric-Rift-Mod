package com.anightdazingzoroark.rift.client.models.entity;

import com.anightdazingzoroark.rift.RiftInitialize;
import com.anightdazingzoroark.rift.server.entities.RiftEgg;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EggModel extends GeoModel<RiftEgg> {
    @Override
    public ResourceLocation getModelResource(RiftEgg object) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/egg.model.json");
    }

    @Override
    public ResourceLocation getTextureResource(RiftEgg object) {
        switch (object.getEggType()) {
            default:
                return new ResourceLocation(RiftInitialize.MODID, "textures/entity/egg/tyrannosaurus_egg.png");
            case 0:
                return new ResourceLocation(RiftInitialize.MODID, "textures/entity/egg/tyrannosaurus_egg.png");
        }
    }

    @Override
    public ResourceLocation getAnimationResource(RiftEgg animatable) {
        return null;
    }
}
