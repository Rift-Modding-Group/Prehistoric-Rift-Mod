package anightdazingzoroark.prift.client.model.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
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
        boolean sleeping = object.isIncapacitated();
        if (object.getVariant() >= 0 && object.getVariant() <= 3) {
            return new ResourceLocation(RiftInitialize.MODID, "textures/entities/"+name+"/"+name+"_"+(object.getVariant()+1)+(sleeping ? "_sleep" : "")+".png");
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
