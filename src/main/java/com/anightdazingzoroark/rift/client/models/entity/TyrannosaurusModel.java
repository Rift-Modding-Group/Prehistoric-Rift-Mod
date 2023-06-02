package com.anightdazingzoroark.rift.client.models.entity;

import com.anightdazingzoroark.rift.RiftInitialize;
import com.anightdazingzoroark.rift.server.entities.creatures.TyrannosaurusEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class TyrannosaurusModel extends GeoModel<TyrannosaurusEntity> {
    @Override
    public ResourceLocation getModelResource(TyrannosaurusEntity object) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/tyrannosaurus.model.json");
    }

    @Override
    public ResourceLocation getTextureResource(TyrannosaurusEntity object) {
        if (object.getVariant() <= 3) {
            return new ResourceLocation(RiftInitialize.MODID, "textures/entity/tyrannosaurus/tyrannosaurus_"+(object.getVariant() + 1)+".png");
        }
        else {
            return new ResourceLocation(RiftInitialize.MODID, "textures/entity/tyrannosaurus/tyrannosaurus_1.png");
        }
    }

    @Override
    public ResourceLocation getAnimationResource(TyrannosaurusEntity animatable) {
        return new ResourceLocation(RiftInitialize.MODID, "animations/tyrannosaurus.animation.json");
    }

    @Override
    public void setCustomAnimations(TyrannosaurusEntity entity, long instanceId, AnimationState<TyrannosaurusEntity> animationState) {
        CoreGeoBone neck = this.getAnimationProcessor().getBone("neck");
        CoreGeoBone saddle = this.getAnimationProcessor().getBone("saddle");
        CoreGeoBone headSaddle = this.getAnimationProcessor().getBone("headSaddle");
        CoreGeoBone chest = this.getAnimationProcessor().getBone("chest");
        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        //for looking around
        if (neck != null && !entity.isRoaring()) {
            neck.setRotX((float) Math.max(Math.toRadians(-12.5), Math.min(Math.toRadians(12.5), Math.toRadians(entityData.headPitch()))));
            neck.setRotY((float) Math.max(Math.toRadians(-12.5), Math.min(Math.toRadians(12.5), Math.toRadians(entityData.netHeadYaw()))));
        }

        //show and hide stuff
        saddle.setHidden(true);
        headSaddle.setHidden(true);
        chest.setHidden(true);
    }
}
