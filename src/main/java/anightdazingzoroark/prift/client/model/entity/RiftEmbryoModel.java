package anightdazingzoroark.prift.client.model.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.other.RiftEmbryo;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class RiftEmbryoModel extends AnimatedGeoModel<RiftEmbryo> {
    @Override
    public ResourceLocation getModelLocation(RiftEmbryo riftEmbryo) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/embryo.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(RiftEmbryo riftEmbryo) {
        return new ResourceLocation(RiftInitialize.MODID, "textures/entities/embryo.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(RiftEmbryo riftEmbryo) {
        return null;
    }
}
