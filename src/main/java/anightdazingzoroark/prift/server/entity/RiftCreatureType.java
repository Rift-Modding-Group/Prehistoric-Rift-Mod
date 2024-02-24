package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.client.renderer.entity.*;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.entity.creature.*;
import anightdazingzoroark.prift.server.enums.*;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import java.util.Locale;

import static anightdazingzoroark.prift.server.items.RiftItems.riftEggItem;

public enum RiftCreatureType {
    TYRANNOSAURUS(Tyrannosaurus.class, CreatureCategory.DINOSAUR, CreatureDiet.CARNIVORE, EnergyCategory.SLOW, EnergyRechargeCategory.NORMAL, BlockBreakTier.WOOD, TyrannosaurusRenderer::new, 3670016, 2428687, 450, 1, EggTemperature.VERY_WARM),
    STEGOSAURUS(Stegosaurus.class, CreatureCategory.DINOSAUR, CreatureDiet.HERBIVORE, EnergyCategory.SLOW, EnergyRechargeCategory.SLOW, BlockBreakTier.DIRT, StegosaurusRenderer::new, 1731840, 16743424, 300, 1, EggTemperature.WARM),
    DODO(Dodo.class, CreatureCategory.BIRD, CreatureDiet.HERBIVORE, null, null, null, DodoRenderer::new, 7828853, 6184028, 90, 0.25f, EggTemperature.NEUTRAL),
    TRICERATOPS(Triceratops.class, CreatureCategory.DINOSAUR, CreatureDiet.HERBIVORE, EnergyCategory.SLOW, EnergyRechargeCategory.SLOW, BlockBreakTier.WOOD, TriceratopsRenderer::new, 935177, 3631923, 300, 1, EggTemperature.WARM),
    UTAHRAPTOR(Utahraptor.class, CreatureCategory.DINOSAUR, CreatureDiet.CARNIVORE, EnergyCategory.FAST, EnergyRechargeCategory.FAST, BlockBreakTier.DIRT, UtahraptorRenderer::new, 5855577, 10439936, 180, 0.5f, EggTemperature.COLD),
    APATOSAURUS(Apatosaurus.class, CreatureCategory.DINOSAUR, CreatureDiet.HERBIVORE, EnergyCategory.VERY_SLOW, EnergyRechargeCategory.SLOW, BlockBreakTier.STONE, ApatosaurusRenderer::new, 3160621, 16748800, 450, 1, EggTemperature.VERY_WARM),
    PARASAUROLOPHUS(Parasaurolophus.class, CreatureCategory.DINOSAUR, CreatureDiet.HERBIVORE, EnergyCategory.NORMAL, EnergyRechargeCategory.NORMAL, BlockBreakTier.DIRT, ParasaurolophusRenderer::new, 10055190, 8920579, 300, 1, EggTemperature.COLD),
    DIMETRODON(Dimetrodon.class, CreatureCategory.MAMMAL, CreatureDiet.CARNIVORE, EnergyCategory.SLOW, EnergyRechargeCategory.NORMAL, null, DimetrodonRenderer::new, 10968581, 13198105, 90, 0.5f, EggTemperature.NEUTRAL),
    COELACANTH(Coelacanth.class, CreatureCategory.FISH, CreatureDiet.INSECTIVORE, null, null, null, CoelacanthRenderer::new, 1329530, 1857680, 0, 0, null),
    MEGAPIRANHA(Megapiranha.class, CreatureCategory.FISH, CreatureDiet.CARNIVORE, null, null, null, MegapiranhaRenderer::new, 8421504, 10226700, 0, 0, null);

    private final Class<? extends RiftCreature> creature;
    private final CreatureCategory creatureCategory;
    private final CreatureDiet creatureDiet;
    private final EnergyCategory energyCategory;
    private final EnergyRechargeCategory energyRechargeCategory;
    private final BlockBreakTier blockBreakTier;
    private final IRenderFactory renderFactory;
    private final int eggPrimary;
    private final int eggSecondary;
    private final int hatchTime; //in seconds
    private final float eggScale;
    private final EggTemperature eggTemperature;
    public Item eggItem;
    public final String friendlyName;

    RiftCreatureType(Class<? extends RiftCreature> creature, CreatureCategory creatureCategory, CreatureDiet creatureDiet, EnergyCategory energyCategory, EnergyRechargeCategory energyRechargeCategory, BlockBreakTier blockBreakTier, IRenderFactory renderFactory,  int eggPrimary, int eggSecondary, int hatchTime, float eggScale, EggTemperature eggTemperature) {
        this.creature = creature;
        this.creatureCategory = creatureCategory;
        this.creatureDiet = creatureDiet;
        this.energyCategory = energyCategory;
        this.energyRechargeCategory = energyRechargeCategory;
        this.blockBreakTier = blockBreakTier;
        this.friendlyName = this.name().toUpperCase(Locale.ENGLISH).substring(0, 1) + this.name().toLowerCase().substring(1);
        this.renderFactory = renderFactory;
        this.eggPrimary = eggPrimary;
        this.eggSecondary = eggSecondary;
        this.hatchTime = hatchTime;
        this.eggScale = eggScale;
        this.eggTemperature = eggTemperature;
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

    public BlockBreakTier getBlockBreakTier() {
        return this.blockBreakTier;
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
        return GeneralConfig.quickEggHatch ? 5 : this.hatchTime;
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

    public EggTemperature getEggTemperature() {
        return this.eggTemperature;
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
            if (creature.getCreatureCategory().equals(CreatureCategory.DINOSAUR) || creature.getCreatureCategory().equals(CreatureCategory.REPTILE) || creature.getCreatureCategory().equals(CreatureCategory.BIRD) || creature.equals(DIMETRODON)) {
                creature.eggItem = riftEggItem(creature.name().toLowerCase()+"_egg", creature);
            }
        }
    }
}
