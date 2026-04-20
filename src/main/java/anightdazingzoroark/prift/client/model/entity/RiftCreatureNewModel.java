package anightdazingzoroark.prift.client.model.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creaturenew.RiftCreatureNew;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;

public class RiftCreatureNewModel extends AnimatedGeoModel<RiftCreatureNew> {
    @Override
    public ResourceLocation getModelLocation(RiftCreatureNew riftCreatureNew) {
        String name = riftCreatureNew.getCreatureType().getName();
        return new ResourceLocation(RiftInitialize.MODID, "geo/"+name+".model.json");
    }

    @Override
    public ResourceLocation getTextureLocation(RiftCreatureNew riftCreatureNew) {
        String name = riftCreatureNew.getCreatureType().getName();
        //note: is like this as color variants will be removed soon
        return new ResourceLocation(RiftInitialize.MODID, "textures/entities/"+name+"/"+name+"_1.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(RiftCreatureNew riftCreatureNew) {
        String name = riftCreatureNew.getCreatureType().getName();
        return new ResourceLocation(RiftInitialize.MODID, "animations/"+name+".animation.json");
    }
}
