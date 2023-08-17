package anightdazingzoroark.rift.server.entity;

import anightdazingzoroark.rift.server.entity.creature.Tyrannosaurus;
import net.minecraft.world.World;

public enum RiftCreatureType {
    TYRANNOSAURUS(Tyrannosaurus.class, CreatureCategory.DINOSAUR);

    private final Class<? extends RiftCreature> creature;
    private final CreatureCategory creatureCategory;

    RiftCreatureType(Class<? extends RiftCreature> creature, CreatureCategory creatureCategory) {
        this.creature = creature;
        this.creatureCategory = creatureCategory;
    }

    public Class<? extends RiftCreature> getCreature() {
        return this.creature;
    }

    public CreatureCategory getCreatureCategory() {
        return this.creatureCategory;
    }

    public RiftCreature invokeClass(World world) {
        RiftCreature entity = null;
        if (RiftCreature.class.isAssignableFrom(this.creature)) {
            try {
                entity = this.creature.getDeclaredConstructor(World.class).newInstance(world);
            }
            catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
        if (entity == null) {
            entity = new Tyrannosaurus(world);
        }
        return entity;
    }
}
