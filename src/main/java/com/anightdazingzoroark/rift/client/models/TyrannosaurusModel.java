package com.anightdazingzoroark.rift.client.models;

import com.anightdazingzoroark.rift.InitializeServer;
import com.anightdazingzoroark.rift.entities.TyrannosaurusEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class TyrannosaurusModel extends AnimatedGeoModel<TyrannosaurusEntity> {
    private static final Identifier MODEL = InitializeServer.id("geo/tyrannosaurus.geo.json");

    private static final Identifier TEXTURE_ONE = InitializeServer.id("textures/entities/tyrannosaurus_1.png");
    private static final Identifier TEXTURE_TWO = InitializeServer.id("textures/entities/tyrannosaurus_2.png");
    private static final Identifier TEXTURE_THREE = InitializeServer.id("textures/entities/tyrannosaurus_3.png");
    private static final Identifier TEXTURE_FOUR = InitializeServer.id("textures/entities/tyrannosaurus_4.png");

    private static final Identifier ANIMATION = InitializeServer.id("animations/tyrannosaurus.animation.json");

    @Override
    public Identifier getModelLocation(TyrannosaurusEntity object) {
        return MODEL;
    }

    @Override
    public Identifier getTextureLocation(TyrannosaurusEntity object) {
        return TEXTURE_ONE;
    }

    @Override
    public Identifier getAnimationFileLocation(TyrannosaurusEntity animatable) {
        return ANIMATION;
    }
}
