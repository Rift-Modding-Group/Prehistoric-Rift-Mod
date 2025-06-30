package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.ridePositionLogic.DynamicRidePosLinker;
import net.minecraft.util.ResourceLocation;

public class RiftCreatureRidePosLinker extends DynamicRidePosLinker<RiftCreature> {
    @Override
    public ResourceLocation getModelLocation(RiftCreature creature) {
        String name = creature.creatureType.name().toLowerCase();
        return new ResourceLocation(RiftInitialize.MODID, "geo/"+name+".model.json");
    }
}
