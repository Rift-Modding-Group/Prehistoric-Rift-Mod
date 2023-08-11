package anightdazingzoroark.rift.client.model;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.server.entity.creature.Tyrannosaurus;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class TyrannosaurusModel extends AnimatedGeoModel<Tyrannosaurus> {
    @Override
    public ResourceLocation getModelLocation(Tyrannosaurus object) {
        return new ResourceLocation(RiftInitialize.MODID, "geo/tyrannosaurus.model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(Tyrannosaurus object) {
        if (object.getVariant() >= 0 && object.getVariant() <= 3) {
            return new ResourceLocation(RiftInitialize.MODID, "textures/entities/tyrannosaurus/tyrannosaurus_"+(object.getVariant()+1)+".png");
        }
        else {
            return new ResourceLocation(RiftInitialize.MODID, "textures/entities/tyrannosaurus_1.png");
        }
    }

    @Override
    public ResourceLocation getAnimationFileLocation(Tyrannosaurus animatable) {
        return new ResourceLocation(RiftInitialize.MODID, "animations/tyrannosaurus.animation.json");
    }
}
