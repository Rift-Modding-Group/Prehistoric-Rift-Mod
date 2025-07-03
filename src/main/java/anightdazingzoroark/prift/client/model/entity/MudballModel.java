package anightdazingzoroark.prift.client.model.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.projectile.Mudball;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;

public class MudballModel extends AnimatedGeoModel<Mudball> {
    @Override
    public ResourceLocation getModelLocation(Mudball mudball) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/dilophosaurus_spit.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(Mudball mudball) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/entities/mudball.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(Mudball mudball) {
        return null;
    }
}
