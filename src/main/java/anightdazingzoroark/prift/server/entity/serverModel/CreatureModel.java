package anightdazingzoroark.prift.server.entity.serverModel;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CreatureModel extends AnimatedGeoModel<RiftCreature> {
    @Override
    @NotNull
    public String getModId() {
        return RiftInitialize.MODID;
    }

    @Override
    public String getModelIdentifier(RiftCreature riftCreature) {
        return "geometry."+riftCreature.getCreatureType().getName();
    }

    @Override
    public String getTextureLocation(RiftCreature riftCreature) {
        String name = riftCreature.getCreatureType().getName();
        //note: is like this as color variants will be removed soon
        return "entities/"+name+"/"+name+"_1.png";
    }
    @Override
    @NotNull
    public List<String> getAnimationIdentifiers(RiftCreature creature) {
        return creature.getAnimationNames();
    }
}
