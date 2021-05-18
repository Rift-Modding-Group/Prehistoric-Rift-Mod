package com.anightdazingzoroark.rift.client.models;

import com.anightdazingzoroark.rift.InitializeServer;
import com.anightdazingzoroark.rift.entities.TyrannosaurusEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class TyrannosaurusModel extends AnimatedGeoModel<TyrannosaurusEntity> {
    private static final Identifier MODEL = InitializeServer.id("models/entity/tyrannosaurus.model.json");

    private static final Identifier TEXTURE_ONE = InitializeServer.id("textures/entities/tyrannosaurus 1.png");
    private static final Identifier TEXTURE_TWO = InitializeServer.id("textures/entities/tyrannosaurus 2.png");
    private static final Identifier TEXTURE_THREE = InitializeServer.id("textures/entities/tyrannosaurus 3.png");
    private static final Identifier TEXTURE_FOUR = InitializeServer.id("textures/entities/tyrannosaurus 4.png");

    private static final Identifier ANIMATION = InitializeServer.id("animations/tyrannosaurus.animation.json");

    @Override
    public Identifier getModelLocation(TyrannosaurusEntity object) {
        return MODEL;
    }

    @Override
    public Identifier getTextureLocation(TyrannosaurusEntity object) {
        Identifier textureVariant = null;
        switch (TyrannosaurusEntity.VARIANT) {
            case 0:
                textureVariant = TEXTURE_ONE;
                break;
            case 1:
                textureVariant = TEXTURE_TWO;
                break;
            case 2:
                textureVariant = TEXTURE_THREE;
                break;
            case 3:
                textureVariant = TEXTURE_FOUR;
                break;
        }

        return textureVariant;
    }

    @Override
    public Identifier getAnimationFileLocation(TyrannosaurusEntity animatable) {
        return ANIMATION;
    }
}
