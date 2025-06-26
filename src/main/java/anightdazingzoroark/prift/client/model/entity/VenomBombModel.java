package anightdazingzoroark.prift.client.model.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.projectile.VenomBombAnimator;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;

public class VenomBombModel extends AnimatedGeoModel<VenomBombAnimator> {
    @Override
    public ResourceLocation getModelLocation(VenomBombAnimator venomBombAnimator) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/venom_bomb.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(VenomBombAnimator venomBombAnimator) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/entities/venom_bomb.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(VenomBombAnimator venomBombAnimator) {
        return new ResourceLocation(RiftInitialize.MODID, "animations/venom_bomb.animation.json");
    }
}
