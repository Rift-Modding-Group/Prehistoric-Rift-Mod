package anightdazingzoroark.prift.client.model.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.projectile.VenomBomb;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;

public class VenomBombModel extends AnimatedGeoModel<VenomBomb> {
    @Override
    public ResourceLocation getModelLocation(VenomBomb venomBombAnimator) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/venom_bomb.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(VenomBomb venomBombAnimator) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/entities/venom_bomb.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(VenomBomb venomBombAnimator) {
        return new ResourceLocation(RiftInitialize.MODID, "animations/venom_bomb.animation.json");
    }
}
