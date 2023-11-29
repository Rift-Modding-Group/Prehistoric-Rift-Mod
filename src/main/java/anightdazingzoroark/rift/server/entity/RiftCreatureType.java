package anightdazingzoroark.rift.server.entity;

import anightdazingzoroark.rift.client.renderer.entity.TriceratopsRenderer;
import anightdazingzoroark.rift.config.DodoConfig;
import anightdazingzoroark.rift.client.renderer.entity.DodoRenderer;
import anightdazingzoroark.rift.client.renderer.entity.StegosaurusRenderer;
import anightdazingzoroark.rift.client.renderer.entity.TyrannosaurusRenderer;
import anightdazingzoroark.rift.config.StegosaurusConfig;
import anightdazingzoroark.rift.config.TyrannosaurusConfig;
import anightdazingzoroark.rift.server.entity.creature.*;
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
    TYRANNOSAURUS(Tyrannosaurus.class, CreatureCategory.DINOSAUR, CreatureDiet.CARNIVORE, EnergyCategory.SLOW, EnergyRechargeCategory.NORMAL, TyrannosaurusRenderer::new, 3670016, 2428687, 450, 1),
    STEGOSAURUS(Stegosaurus.class, CreatureCategory.DINOSAUR, CreatureDiet.HERBIVORE, EnergyCategory.SLOW, EnergyRechargeCategory.SLOW, StegosaurusRenderer::new, 1731840, 16743424, 300, 1),
    DODO(Dodo.class, CreatureCategory.BIRD, CreatureDiet.HERBIVORE, null, null, DodoRenderer::new, 7828853, 6184028, 90, 0.25f),
    TRICERATOPS(Triceratops.class, CreatureCategory.DINOSAUR, CreatureDiet.HERBIVORE, EnergyCategory.SLOW, EnergyRechargeCategory.SLOW, TriceratopsRenderer::new, 935177, 3631923, 300, 1);

    private final Class<? extends RiftCreature> creature;
    private final CreatureCategory creatureCategory;
    private final CreatureDiet creatureDiet;
    private final EnergyCategory energyCategory;
    private final EnergyRechargeCategory energyRechargeCategory;
    private final IRenderFactory renderFactory;
    private final int eggPrimary;
    private final int eggSecondary;
    private final int hatchTime; //in seconds
    private final float eggScale;
    public Item eggItem;
    public final String friendlyName;

    RiftCreatureType(Class<? extends RiftCreature> creature, CreatureCategory creatureCategory, CreatureDiet creatureDiet, EnergyCategory energyCategory, EnergyRechargeCategory energyRechargeCategory, IRenderFactory renderFactory,  int eggPrimary, int eggSecondary, int hatchTime, float eggScale) {
        this.creature = creature;
        this.creatureCategory = creatureCategory;
        this.creatureDiet = creatureDiet;
        this.energyCategory = energyCategory;
        this.energyRechargeCategory = energyRechargeCategory;
        this.friendlyName = this.name().toUpperCase(Locale.ENGLISH).substring(0, 1) + this.name().toLowerCase().substring(1);
        this.renderFactory = renderFactory;
        this.eggPrimary = eggPrimary;
        this.eggSecondary = eggSecondary;
        this.hatchTime = hatchTime;
        this.eggScale = eggScale;
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

    public int getEggPrimary() {
        return this.eggPrimary;
    }

    public int getEggSecondary() {
        return this.eggSecondary;
    }

    public int getHatchTime() {
        return this.hatchTime;
    }

    public float getEggScale() {
        return this.eggScale;
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
