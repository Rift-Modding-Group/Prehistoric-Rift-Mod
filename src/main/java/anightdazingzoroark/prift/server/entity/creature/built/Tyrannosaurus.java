package anightdazingzoroark.prift.server.entity.creature.built;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.ray.IRayCreator;
import net.minecraft.world.World;

public class Tyrannosaurus extends RiftCreature implements IRayCreator<RiftCreature> {
    public Tyrannosaurus(World worldIn) {
        super(worldIn, "tyrannosaurus");
    }
}
