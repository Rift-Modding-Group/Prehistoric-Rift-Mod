package com.anightdazingzoroark.rift.client.models;

import com.anightdazingzoroark.rift.InitializeServer;
import com.anightdazingzoroark.rift.entities.RiftEgg;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;


public class EggModel extends AnimatedGeoModel<RiftEgg> {
    private static final Identifier MODEL = InitializeServer.id("geo/egg.geo.json");
    private static Identifier TEXTURE;

    @Override
    public Identifier getModelLocation(RiftEgg object) {
        return MODEL;
    }

    @Override
    public Identifier getTextureLocation(RiftEgg object) {
        switch (object.getEggType()) {
            case 0:
                TEXTURE = InitializeServer.id("textures/eggs/tyrannosaurus_egg.png");
                break;
            default:
                TEXTURE = InitializeServer.id("textures/eggs/tyrannosaurus_egg.png");
                break;
        }

        return TEXTURE;
    }

    @Override
    public Identifier getAnimationFileLocation(RiftEgg animatable) {
        return null;
    }
}
