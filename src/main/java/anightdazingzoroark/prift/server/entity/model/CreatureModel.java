package anightdazingzoroark.prift.server.entity.model;

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
    @NotNull
    public String getModelIdentifier(RiftCreature riftCreature) {
        return "geometry."+riftCreature.getCreatureType().getName();
    }

    @Override
    @NotNull
    public String getTextureLocation(RiftCreature riftCreature) {
        String name = riftCreature.getCreatureType().getName();
        String sleepNote = riftCreature.getIsSleeping() ? "_sleep" : "";
        return "entities/"+name+"/"+name+sleepNote+".png";
    }
}
