package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.hitbox.EntityHitboxLinker;
import net.minecraft.util.ResourceLocation;

public class RiftCreatureHitboxLinker extends EntityHitboxLinker<RiftCreature> {
    @Override
    public ResourceLocation getHitboxFileLocation(RiftCreature creature) {
        String name = creature.creatureType.name().toLowerCase();
        return new ResourceLocation(RiftInitialize.MODID, "hitboxes/"+name+".json");
    }
}
