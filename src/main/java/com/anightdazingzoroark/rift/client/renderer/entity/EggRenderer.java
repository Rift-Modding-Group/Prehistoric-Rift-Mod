package com.anightdazingzoroark.rift.client.renderer.entity;

import com.anightdazingzoroark.rift.RiftInitialize;
import com.anightdazingzoroark.rift.client.models.entity.EggModel;
import com.anightdazingzoroark.rift.server.entities.RiftEgg;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class EggRenderer extends GeoEntityRenderer<RiftEgg> {
    public EggRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new EggModel());
        this.shadowRadius = 0.6F;
    }


    @Override
    public ResourceLocation getTextureLocation(RiftEgg instance) {
        switch (instance.getEggType()) {
            default:
                return new ResourceLocation(RiftInitialize.MODID, "textures/entity/egg/tyrannosaurus_egg.png");
            case 0:
                return new ResourceLocation(RiftInitialize.MODID, "textures/entity/egg/tyrannosaurus_egg.png");
        }
    }

    @Override
    public void render(RiftEgg entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
