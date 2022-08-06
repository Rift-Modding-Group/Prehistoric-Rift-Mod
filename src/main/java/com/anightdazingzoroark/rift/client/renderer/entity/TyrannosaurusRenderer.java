package com.anightdazingzoroark.rift.client.renderer.entity;

import com.anightdazingzoroark.rift.RiftInitialize;
import com.anightdazingzoroark.rift.client.models.entity.TyrannosaurusModel;
import com.anightdazingzoroark.rift.server.entities.creatures.TyrannosaurusEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class TyrannosaurusRenderer extends GeoEntityRenderer<TyrannosaurusEntity> {
    public TyrannosaurusRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new TyrannosaurusModel());
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
    public RenderType getRenderType(TyrannosaurusEntity animatable, float partialTicks, PoseStack stack, @Nullable MultiBufferSource renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
        stack.scale(3.25f, 3.25f, 3.25f);
        return super.getRenderType(animatable, partialTicks, stack, renderTypeBuffer, vertexBuilder, packedLightIn, textureLocation);
    }

    @Override
    public void render(GeoModel model, TyrannosaurusEntity animatable, float partialTicks, RenderType type, PoseStack matrixStackIn, @Nullable MultiBufferSource renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        model.getBone("chest").get().setHidden(true);
        model.getBone("headSaddle").get().setHidden(true);
        model.getBone("saddle").get().setHidden(true);
        super.render(model, animatable, partialTicks, type, matrixStackIn, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
