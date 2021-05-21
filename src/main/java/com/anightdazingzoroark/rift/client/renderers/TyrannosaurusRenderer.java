package com.anightdazingzoroark.rift.client.renderers;

import com.anightdazingzoroark.rift.client.models.TyrannosaurusModel;
import com.anightdazingzoroark.rift.entities.TyrannosaurusEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.*;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderer.geo.GeoEntityRenderer;
import software.bernie.geckolib3.resource.GeckoLibCache;

public class TyrannosaurusRenderer extends GeoEntityRenderer<TyrannosaurusEntity>{
    public TyrannosaurusRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new TyrannosaurusModel());
        this.shadowRadius = 1.5F;
    }

    @Override
    public void render(GeoModel model, TyrannosaurusEntity animatable, float partialTicks, RenderLayer type, MatrixStack matrixStackIn, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        matrixStackIn.scale(1.5F, 1.5F, 1.5F);
        super.render(model, animatable, partialTicks, type, matrixStackIn, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
