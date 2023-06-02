package com.anightdazingzoroark.rift.client.renderer.entity;

import com.anightdazingzoroark.rift.RiftInitialize;
import com.anightdazingzoroark.rift.client.models.entity.TyrannosaurusModel;
import com.anightdazingzoroark.rift.server.entities.creatures.TyrannosaurusEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TyrannosaurusRenderer extends GeoEntityRenderer<TyrannosaurusEntity> {
    public TyrannosaurusRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new TyrannosaurusModel());
        this.shadowRadius = 1F;
    }

    @Override
    public ResourceLocation getTextureLocation(TyrannosaurusEntity instance) {
        if (instance.getVariant() <= 3) {
            return new ResourceLocation(RiftInitialize.MODID, "textures/entity/tyrannosaurus/tyrannosaurus_"+(instance.getVariant() + 1)+".png");
        }
        else {
            return new ResourceLocation(RiftInitialize.MODID, "textures/entity/tyrannosaurus/tyrannosaurus_1.png");
        }
    }

    @Override
    public void render(TyrannosaurusEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.scale(3.25f, 3.25f, 3.25f);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
