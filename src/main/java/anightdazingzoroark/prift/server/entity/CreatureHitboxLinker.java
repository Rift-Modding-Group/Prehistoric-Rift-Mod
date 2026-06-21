package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureHitboxed;
import anightdazingzoroark.riftlib.hitbox.EntityHitboxLinker;
import net.minecraft.util.ResourceLocation;

public class CreatureHitboxLinker extends EntityHitboxLinker<RiftCreatureHitboxed> {
    @Override
    public ResourceLocation getHitboxFileLocation(RiftCreatureHitboxed creature) {
        return new ResourceLocation(RiftInitialize.MODID, "hitboxes/"+creature.getCreatureType().getName()+".json");
    }
}
