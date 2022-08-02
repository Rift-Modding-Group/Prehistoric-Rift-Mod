package com.anightdazingzoroark.rift.client.models;

import com.anightdazingzoroark.rift.InitializeServer;
import com.anightdazingzoroark.rift.entities.Creatures.TyrannosaurusEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;


public class TyrannosaurusModel extends AnimatedGeoModel<TyrannosaurusEntity> {
    private static final Identifier MODEL = InitializeServer.id("geo/tyrannosaurus.geo.json");
    private static Identifier TEXTURE;
    private static final Identifier ANIMATION = InitializeServer.id("animations/tyrannosaurus.animation.json");

    @Override
    public Identifier getModelLocation(TyrannosaurusEntity object) {
        return MODEL;
    }

    @Override
    public Identifier getTextureLocation(TyrannosaurusEntity object) {
        switch (object.getVariant()) {
            case 0:
                TEXTURE = InitializeServer.id("textures/entities/tyrannosaurus/tyrannosaurus_1.png");
                break;
            case 1:
                TEXTURE = InitializeServer.id("textures/entities/tyrannosaurus/tyrannosaurus_2.png");
                break;
            case 2:
                TEXTURE = InitializeServer.id("textures/entities/tyrannosaurus/tyrannosaurus_3.png");
                break;
            case 3:
                TEXTURE = InitializeServer.id("textures/entities/tyrannosaurus/tyrannosaurus_4.png");
                break;
            default:
                TEXTURE = InitializeServer.id("textures/entities/tyrannosaurus/tyrannosaurus_1.png");
                break;
        }

        return TEXTURE;
    }

    @Override
    public Identifier getAnimationFileLocation(TyrannosaurusEntity animatable) {
        return ANIMATION;
    }
}
