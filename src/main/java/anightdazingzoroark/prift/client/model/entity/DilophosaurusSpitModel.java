package anightdazingzoroark.prift.client.model.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.projectile.DilophosaurusSpitAnimator;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class DilophosaurusSpitModel extends AnimatedGeoModel<DilophosaurusSpitAnimator> {
    @Override
    public ResourceLocation getModelLocation(DilophosaurusSpitAnimator dilophosaurusSpitAnimator) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/dilophosaurus_spit.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(DilophosaurusSpitAnimator dilophosaurusSpitAnimator) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/entities/dilophosaurus_spit.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(DilophosaurusSpitAnimator dilophosaurusSpitAnimator) {
        return null;
    }
}
