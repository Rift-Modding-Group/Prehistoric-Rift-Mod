package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.hitboxLogic.EntityHitboxLinker;
import net.minecraft.util.ResourceLocation;

public class RiftCreatureHitboxLinker extends EntityHitboxLinker<RiftCreature> {
    public RiftCreatureHitboxLinker() {
        System.out.println("hello world");
    }

    @Override
    public ResourceLocation getModelLocation(RiftCreature creature) {
        String name = creature.creatureType.name().toLowerCase();
        return new ResourceLocation(RiftInitialize.MODID, "geo/"+name+".model.json");
    }

    @Override
    public ResourceLocation getHitboxFileLocation(RiftCreature creature) {
        String name = creature.creatureType.name().toLowerCase();
        System.out.println("hello, from "+name);
        return new ResourceLocation(RiftInitialize.MODID, "hitboxes/"+name+".json");
    }
}
