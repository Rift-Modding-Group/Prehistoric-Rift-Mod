package anightdazingzoroark.prift.client.model.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.projectile.ThrownBola;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;

public class ThrownBolaModel extends AnimatedGeoModel<ThrownBola> {
    @Override
    public ResourceLocation getModelLocation(ThrownBola thrownBola) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/bola.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownBola thrownBola) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/entities/bola.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ThrownBola thrownBola) {
        return new ResourceLocation(RiftInitialize.MODID, "animations/bola.animation.json");
    }
}
