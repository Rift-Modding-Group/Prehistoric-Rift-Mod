package com.anightdazingzoroark.rift.client.models.entity;

import com.anightdazingzoroark.rift.RiftInitialize;
import com.anightdazingzoroark.rift.server.entities.creatures.TyrannosaurusEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public class TyrannosaurusModel extends AnimatedGeoModel<TyrannosaurusEntity> {
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void setLivingAnimations(TyrannosaurusEntity entity, Integer uniqueID, @Nullable AnimationEvent customPredicate) {
        super.setLivingAnimations(entity, uniqueID, customPredicate);
        IBone neck = this.getAnimationProcessor().getBone("neck");
        EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);

        //for looking around
        if (neck != null) {
            neck.setRotationX((float) Math.max(Math.toRadians(-12.5), Math.min(Math.toRadians(12.5), Math.toRadians(extraData.headPitch))));
            neck.setRotationY((float) Math.max(Math.toRadians(-12.5), Math.min(Math.toRadians(12.5), Math.toRadians(extraData.netHeadYaw))));
        }
    }
}
