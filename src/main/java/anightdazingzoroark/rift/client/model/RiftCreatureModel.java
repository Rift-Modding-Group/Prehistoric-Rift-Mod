package anightdazingzoroark.rift.client.model;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class RiftCreatureModel extends AnimatedGeoModel<RiftCreature> {
    @Override
    public ResourceLocation getModelLocation(RiftCreature object) {
        String name = object.creatureType.name().toLowerCase();
        return new ResourceLocation(RiftInitialize.MODID, "geo/"+name+".model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(RiftCreature object) {
        String name = object.creatureType.name().toLowerCase();
        if (object.getVariant() >= 0 && object.getVariant() <= 3) {
            return new ResourceLocation(RiftInitialize.MODID, "textures/entities/"+name+"/"+name+"_"+(object.getVariant()+1)+".png");
        }
        else {
            return new ResourceLocation(RiftInitialize.MODID, "textures/entities/"+name+"_1.png");
        }
    }

    @Override
    public ResourceLocation getAnimationFileLocation(RiftCreature animatable) {
        String name = animatable.creatureType.name().toLowerCase();
        return new ResourceLocation(RiftInitialize.MODID, "animations/"+name+".animation.json");
    }
}
