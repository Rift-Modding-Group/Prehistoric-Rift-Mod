package com.anightdazingzoroark.rift.client.renderers;

import com.anightdazingzoroark.rift.client.models.EggModel;
import com.anightdazingzoroark.rift.entities.RiftEgg;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class EggRenderer extends GeoEntityRenderer<RiftEgg> {
    public EggRenderer(EntityRendererFactory.Context renderManagerIn) {
        super(renderManagerIn, new EggModel());
    }

    @Override
    public void render(GeoModel model, RiftEgg animatable, float partialTicks, RenderLayer type, MatrixStack matrixStackIn, VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        super.render(model, animatable, partialTicks, type, matrixStackIn, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
