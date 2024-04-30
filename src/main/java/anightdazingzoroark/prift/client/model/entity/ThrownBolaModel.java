package anightdazingzoroark.prift.client.model.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.projectile.ThrownBolaAnimator;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ThrownBolaModel extends AnimatedGeoModel<ThrownBolaAnimator> {
    @Override
    public ResourceLocation getModelLocation(ThrownBolaAnimator thrownBolaAnimator) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/bola.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownBolaAnimator thrownBolaAnimator) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/entities/bola.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ThrownBolaAnimator thrownBolaAnimator) {
        return new ResourceLocation(RiftInitialize.MODID, "animations/bola.animation.json");
    }
}
