package com.anightdazingzoroark.rift.client.renderers;

import com.anightdazingzoroark.rift.client.models.TyrannosaurusModel;
import com.anightdazingzoroark.rift.entities.Creatures.TyrannosaurusEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class TyrannosaurusRenderer extends GeoEntityRenderer<TyrannosaurusEntity> {
    public TyrannosaurusRenderer(EntityRendererFactory.Context renderManagerIn) {
        super(renderManagerIn, new TyrannosaurusModel());
    }

    @Override
    public void render(GeoModel model, TyrannosaurusEntity animatable, float partialTicks, RenderLayer type, MatrixStack matrixStackIn, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        matrixStackIn.scale(1.5F, 1.5F, 1.5F);
        model.getBone("chest").get().setHidden(true);
        model.getBone("headSaddle").get().setHidden(true);
        model.getBone("saddle").get().setHidden(true);
        super.render(model, animatable, partialTicks, type, matrixStackIn, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
