package anightdazingzoroark.prift.server.entity.creature.built;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureHitboxed;
import anightdazingzoroark.riftlib.hitbox.HitboxDefinitionList;
import anightdazingzoroark.riftlib.hitbox.IMultiHitboxUser;
import anightdazingzoroark.riftlib.ray.IRayCreator;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class Tyrannosaurus extends RiftCreatureHitboxed implements IRayCreator<RiftCreature> {
    public Tyrannosaurus(World worldIn) {
        super(worldIn, "tyrannosaurus");
    }
}
