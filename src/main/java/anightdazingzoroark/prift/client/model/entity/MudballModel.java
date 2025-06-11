package anightdazingzoroark.prift.client.model.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.projectile.DilophosaurusSpitAnimator;
import anightdazingzoroark.prift.server.entity.projectile.MudballAnimator;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class MudballModel extends AnimatedGeoModel<MudballAnimator> {
    @Override
    public ResourceLocation getModelLocation(MudballAnimator mudballAnimator) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/dilophosaurus_spit.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(MudballAnimator mudballAnimator) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/entities/mudball.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(MudballAnimator mudballAnimator) {
        return null;
    }
}
