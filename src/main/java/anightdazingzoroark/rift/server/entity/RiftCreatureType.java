package anightdazingzoroark.rift.server.entity;

import anightdazingzoroark.rift.client.renderer.entity.TyrannosaurusRenderer;
import anightdazingzoroark.rift.server.entity.creature.Tyrannosaurus;
import anightdazingzoroark.rift.server.enums.CreatureCategory;
import anightdazingzoroark.rift.server.enums.CreatureDiet;
import anightdazingzoroark.rift.server.enums.EnergyCategory;
import anightdazingzoroark.rift.server.enums.EnergyRechargeCategory;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import java.util.Locale;

import static anightdazingzoroark.rift.server.items.RiftItems.riftEggItem;

public enum RiftCreatureType {
    TYRANNOSAURUS(Tyrannosaurus.class, CreatureCategory.DINOSAUR, CreatureDiet.CARNIVORE, EnergyCategory.SLOW, EnergyRechargeCategory.NORMAL, TyrannosaurusRenderer::new);

    private final Class<? extends RiftCreature> creature;
    private final CreatureCategory creatureCategory;
    private final CreatureDiet creatureDiet;
    private final EnergyCategory energyCategory;
    private final EnergyRechargeCategory energyRechargeCategory;
    private final IRenderFactory renderFactory;
    public Item eggItem;
    public final String friendlyName;

    RiftCreatureType(Class<? extends RiftCreature> creature, CreatureCategory creatureCategory, CreatureDiet creatureDiet, EnergyCategory energyCategory, EnergyRechargeCategory energyRechargeCategory, IRenderFactory renderFactory) {
        this.creature = creature;
        this.creatureCategory = creatureCategory;
        this.creatureDiet = creatureDiet;
        this.energyCategory = energyCategory;
        this.energyRechargeCategory = energyRechargeCategory;
        this.friendlyName = this.name().toUpperCase(Locale.ENGLISH).substring(0, 1) + this.name().toLowerCase().substring(1);
        this.renderFactory = renderFactory;
    }

    public Class<? extends RiftCreature> getCreature() {
        return this.creature;
    }

    public CreatureCategory getCreatureCategory() {
        return this.creatureCategory;
    }

    public CreatureDiet getCreatureDiet() {
        return this.creatureDiet;
    }

    public EnergyCategory getEnergyCategory() {
        return this.energyCategory;
    }

    public EnergyRechargeCategory getEnergyRechargeCategory() {
        return this.energyRechargeCategory;
    }

    public IRenderFactory getRenderFactory() {
        return this.renderFactory;
    }

    public int getMaxEnergyModMovement() {
        switch (this.energyCategory) {
            case FAST:
                return 60;
            case NORMAL:
                return 100;
            case SLOW:
                return 160;
            case VERY_SLOW:
                return 200;
        }
        return 0;
    }

    public int getMaxEnergyModAction() {
        switch (this.energyCategory) { //all of these r in how many times an action was done
            case FAST:
                return 5;
            case NORMAL:
                return 8;
            case SLOW:
                return 10;
            case VERY_SLOW:
                return 12;
        }
        return 0;
    }

    public int getMaxEnergyRegenMod() {
        switch (this.energyRechargeCategory) { //all of these r in seconds that r converted to ticks
            case FAST:
                return 10;
            case NORMAL:
                return 40;
            case SLOW:
                return 80;
        }
        return 0;
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
