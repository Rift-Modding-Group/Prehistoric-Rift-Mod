package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.renderer.entity.*;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.entity.creature.*;
import anightdazingzoroark.prift.server.enums.*;
import anightdazingzoroark.prift.server.items.RiftItems;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import java.util.Locale;

public enum RiftCreatureType {
    TYRANNOSAURUS(Tyrannosaurus.class, CreatureCategory.DINOSAUR, CreatureDiet.CARNIVORE, EnergyCategory.SLOW, EnergyRechargeCategory.NORMAL, BlockBreakTier.WOOD, LevelupRate.SLOW, TyrannosaurusRenderer::new, 3670016, 2428687, 450, 1, EggTemperature.VERY_WARM),
    STEGOSAURUS(Stegosaurus.class, CreatureCategory.DINOSAUR, CreatureDiet.HERBIVORE, EnergyCategory.SLOW, EnergyRechargeCategory.SLOW, BlockBreakTier.DIRT, LevelupRate.NORMAL, StegosaurusRenderer::new, 1731840, 16743424, 300, 1, EggTemperature.WARM),
    DODO(Dodo.class, CreatureCategory.BIRD, CreatureDiet.HERBIVORE, null, null, null, null, DodoRenderer::new, 7828853, 6184028, 90, 0.25f, EggTemperature.NEUTRAL),
    TRICERATOPS(Triceratops.class, CreatureCategory.DINOSAUR, CreatureDiet.HERBIVORE, EnergyCategory.SLOW, EnergyRechargeCategory.SLOW, BlockBreakTier.WOOD, LevelupRate.NORMAL, TriceratopsRenderer::new, 935177, 3631923, 300, 1, EggTemperature.WARM),
    UTAHRAPTOR(Utahraptor.class, CreatureCategory.DINOSAUR, CreatureDiet.CARNIVORE, EnergyCategory.FAST, EnergyRechargeCategory.FAST, BlockBreakTier.DIRT, LevelupRate.FAST, UtahraptorRenderer::new, 5855577, 10439936, 180, 0.5f, EggTemperature.COLD),
    APATOSAURUS(Apatosaurus.class, CreatureCategory.DINOSAUR, CreatureDiet.HERBIVORE, EnergyCategory.VERY_SLOW, EnergyRechargeCategory.SLOW, BlockBreakTier.STONE, LevelupRate.VERY_SLOW, ApatosaurusRenderer::new, 3160621, 16748800, 450, 1, EggTemperature.VERY_WARM),
    PARASAUROLOPHUS(Parasaurolophus.class, CreatureCategory.DINOSAUR, CreatureDiet.HERBIVORE, EnergyCategory.NORMAL, EnergyRechargeCategory.NORMAL, BlockBreakTier.DIRT, LevelupRate.FAST, ParasaurolophusRenderer::new, 10055190, 8920579, 300, 1, EggTemperature.COLD),
    DIMETRODON(Dimetrodon.class, CreatureCategory.MAMMAL, CreatureDiet.CARNIVORE, EnergyCategory.SLOW, EnergyRechargeCategory.NORMAL, null, LevelupRate.NORMAL, DimetrodonRenderer::new, 10968581, 13198105, 90, 0.5f, EggTemperature.NEUTRAL),
    COELACANTH(Coelacanth.class, CreatureCategory.FISH, CreatureDiet.INSECTIVORE, null, null, null, null, CoelacanthRenderer::new, 1329530, 1857680, 0, 0, null),
    MEGAPIRANHA(Megapiranha.class, CreatureCategory.FISH, CreatureDiet.CARNIVORE, null, null, null, null, MegapiranhaRenderer::new, 8421504, 10226700, 0, 0, null),
    SARCOSUCHUS(Sarcosuchus.class, CreatureCategory.REPTILE, CreatureDiet.CARNIVORE, EnergyCategory.FAST, EnergyRechargeCategory.SLOW, BlockBreakTier.WOOD, LevelupRate.NORMAL,  SarcosuchusRenderer::new, 2302246, 2627379, 300, 0.5f, EggTemperature.COLD),
    ANOMALOCARIS(Anomalocaris.class, CreatureCategory.INVERTEBRATE, CreatureDiet.CARNIVORE, EnergyCategory.SLOW, EnergyRechargeCategory.SLOW, BlockBreakTier.DIRT, LevelupRate.NORMAL, AnomalocarisRenderer::new, 10892050, 12270358, 300, 1f, null),
    SAUROPHAGANAX(Saurophaganax.class, CreatureCategory.DINOSAUR, CreatureDiet.INSECTIVORE, EnergyCategory.SLOW, EnergyRechargeCategory.SLOW, BlockBreakTier.WOOD, LevelupRate.NORMAL, SaurophaganaxRenderer::new, 986895, 16737280, 450, 1f, EggTemperature.COLD),
    DIREWOLF(Direwolf.class, CreatureCategory.MAMMAL, CreatureDiet.CARNIVORE, EnergyCategory.NORMAL, EnergyRechargeCategory.FAST, BlockBreakTier.DIRT, LevelupRate.FAST, DirewolfRenderer::new, 8421504, 10066329, 0, 0, null),
    MEGALOCEROS(Megaloceros.class, CreatureCategory.MAMMAL, CreatureDiet.HERBIVORE, EnergyCategory.FAST, EnergyRechargeCategory.NORMAL, BlockBreakTier.DIRT, LevelupRate.FAST, MegalocerosRenderer::new, 6048296, 4666924, 0, 0, null);

    private final Class<? extends RiftCreature> creature;
    private final CreatureCategory creatureCategory;
    private final CreatureDiet creatureDiet;
    private final EnergyCategory energyCategory;
    private final EnergyRechargeCategory energyRechargeCategory;
    private final BlockBreakTier blockBreakTier;
    private final LevelupRate levelupRate;
    private final IRenderFactory renderFactory;
    private final int eggPrimary;
    private final int eggSecondary;
    private final int hatchTime; //in seconds
    private final float eggScale;
    private final EggTemperature eggTemperature;
    public Item eggItem;
    public Item sacItem;
    public final String friendlyName;

    RiftCreatureType(Class<? extends RiftCreature> creature, CreatureCategory creatureCategory, CreatureDiet creatureDiet, EnergyCategory energyCategory, EnergyRechargeCategory energyRechargeCategory, BlockBreakTier blockBreakTier, LevelupRate levelupRate, IRenderFactory renderFactory,  int eggPrimary, int eggSecondary, int hatchTime, float eggScale, EggTemperature eggTemperature) {
        this.creature = creature;
        this.creatureCategory = creatureCategory;
        this.creatureDiet = creatureDiet;
        this.energyCategory = energyCategory;
        this.energyRechargeCategory = energyRechargeCategory;
        this.blockBreakTier = blockBreakTier;
        this.levelupRate = levelupRate;
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

    public LevelupRate getLevelupRate() {
        return this.levelupRate;
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

    public int getMaxEnergyModMovement(int level) {
        int adder = 0;
        switch (this.energyCategory) {
            case FAST:
                adder = 60;
                break;
            case NORMAL:
                adder = 100;
                break;
            case SLOW:
                adder = 160;
                break;
            case VERY_SLOW:
                adder = 200;
                break;
        }
        return adder + (int)((double)level * 0.3D);
    }

    public int getMaxEnergyModAction(int level) {
        int adder = 0;
        switch (this.energyCategory) { //all of these r in how many times an action was done
            case FAST:
                adder = 5;
                break;
            case NORMAL:
                adder = 8;
                break;
            case SLOW:
                adder = 10;
                break;
            case VERY_SLOW:
                adder = 12;
                break;
        }
        return adder + (int)((double)level * 0.1);
    }

    public int getMaxEnergyRegenMod(int level) {
        int adder = 0;
        double slope = 0;
        switch (this.energyRechargeCategory) { //all of these r in seconds that r converted to ticks
            case FAST:
                adder = 10;
                slope = 0.1;
                break;
            case NORMAL:
                adder = 40;
                slope = 0.25;
                break;
            case SLOW:
                adder = 80;
                slope = 0.5;
                break;
        }
        return RiftUtil.clamp(adder - (int)((double)level * slope), 1, 80);
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
        if (entity == null) entity = new Tyrannosaurus(world);
        return entity;
    }

    public String getTranslatedName() {
        return I18n.format("entity."+this.name().toLowerCase()+".name");
    }

    public static RiftCreatureType safeValOf(String string) {
        for (RiftCreatureType creatureType : RiftCreatureType.values()) {
            if (creatureType.name().equalsIgnoreCase(string)) {
                return creatureType;
            }
        }
        return null;
    }

    public static void registerEggs() {
        for (RiftCreatureType creature : RiftCreatureType.values()) {
            if (creature.getCreatureCategory().equals(CreatureCategory.DINOSAUR) || creature.getCreatureCategory().equals(CreatureCategory.REPTILE) || creature.getCreatureCategory().equals(CreatureCategory.BIRD) || creature.equals(DIMETRODON)) {
                creature.eggItem = RiftItems.riftEggItem(creature.name().toLowerCase()+"_egg", creature);
            }
        }
    }

    public static void registerSacs() {
        for (RiftCreatureType creature : RiftCreatureType.values()) {
            if (creature.getCreatureCategory().equals(CreatureCategory.INVERTEBRATE)) {
                creature.sacItem = RiftItems.riftSacItem(creature.name().toLowerCase()+"_sac", creature);
            }
        }
    }
}
