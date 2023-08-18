package anightdazingzoroark.rift.server.entity;

import anightdazingzoroark.rift.server.entity.creature.Tyrannosaurus;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import java.util.Locale;

import static anightdazingzoroark.rift.server.items.RiftItems.riftEggItem;

public enum RiftCreatureType {
    TYRANNOSAURUS(Tyrannosaurus.class, CreatureCategory.DINOSAUR);

    private final Class<? extends RiftCreature> creature;
    private final CreatureCategory creatureCategory;
    public Item eggItem;
    public final String friendlyName;

    RiftCreatureType(Class<? extends RiftCreature> creature, CreatureCategory creatureCategory) {
        this.creature = creature;
        this.creatureCategory = creatureCategory;
        this.friendlyName = this.name().toUpperCase(Locale.ENGLISH).substring(0, 1) + this.name().toLowerCase().substring(1);
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

    public static void registerEggs() {
        for (RiftCreatureType creature : RiftCreatureType.values()) {
            if (creature.getCreatureCategory().equals(CreatureCategory.DINOSAUR) || creature.getCreatureCategory().equals(CreatureCategory.REPTILE) || creature.getCreatureCategory().equals(CreatureCategory.BIRD)) {
                creature.eggItem = riftEggItem(creature.name().toLowerCase()+"_egg", creature);
            }
        }
    }
}
