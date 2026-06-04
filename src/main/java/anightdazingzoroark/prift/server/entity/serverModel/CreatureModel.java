package anightdazingzoroark.prift.server.entity.serverModel;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;

public class CreatureModel extends AnimatedGeoModel<RiftCreature> {
    @Override
    public ResourceLocation getModelLocation(RiftCreature riftCreature) {
        String name = riftCreature.getCreatureType().getName();
        return new ResourceLocation(RiftInitialize.MODID, "geo/"+name+".model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(RiftCreature riftCreature) {
        String name = riftCreature.getCreatureType().getName();
        //note: is like this as color variants will be removed soon
        return new ResourceLocation(RiftInitialize.MODID, "textures/entities/"+name+"/"+name+"_1.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(RiftCreature riftCreature) {
        String name = riftCreature.getCreatureType().getName();
        return new ResourceLocation(RiftInitialize.MODID, "animations/"+name+".animation.json");
    }
}
