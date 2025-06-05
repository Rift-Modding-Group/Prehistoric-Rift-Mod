package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.config.*;
import anightdazingzoroark.prift.server.entity.creature.*;
import anightdazingzoroark.prift.server.enums.*;
import anightdazingzoroark.prift.server.items.RiftItems;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum RiftCreatureType {
    TYRANNOSAURUS(
            Tyrannosaurus.class,
            TyrannosaurusConfig.class,
            true,
            false,
            true,
            true,
            false,
            CreatureCategory.DINOSAUR,
            EnergyRechargeCategory.SLOW,
            CreatureDiet.CARNIVORE,
            LevelupRate.SLOW,
            new Behavior[]{Behavior.DOCILE, Behavior.AGGRESSIVE, Behavior.BLOCK_BREAKER},
            3670016,
            2428687,
            450,
            1,
            EggTemperature.VERY_WARM
    ),
    STEGOSAURUS(
            Stegosaurus.class,
            StegosaurusConfig.class,
            true,
            true,
            true,
            true,
            false,
            CreatureCategory.DINOSAUR,
            EnergyRechargeCategory.SLOW,
            CreatureDiet.HERBIVORE,
            LevelupRate.NORMAL,
            new Behavior[]{Behavior.DOCILE, Behavior.HERDER, Behavior.BLOCK_BREAKER},
            1731840,
            16743424,
            300,
            1,
            EggTemperature.WARM
    ),
    DODO(
            Dodo.class,
            DodoConfig.class,
            false,
            false,
            true,
            false,
            false,
            CreatureCategory.BIRD,
            EnergyRechargeCategory.SLOW,
            CreatureDiet.HERBIVORE,
            null,
            new Behavior[]{},
            7828853,
            6184028,
            90,
            0.25f,
            EggTemperature.NEUTRAL
    ),
    TRICERATOPS(
            Triceratops.class,
            TriceratopsConfig.class,
            true,
            true,
            true,
            true,
            false,
            CreatureCategory.DINOSAUR,
            EnergyRechargeCategory.SLOW,
            CreatureDiet.HERBIVORE,
            LevelupRate.NORMAL,
            new Behavior[]{Behavior.DOCILE, Behavior.HERDER, Behavior.BLOCK_BREAKER},
            935177,
            3631923,
            300,
            1,
            EggTemperature.WARM
    ),
    UTAHRAPTOR(
            Utahraptor.class,
            UtahraptorConfig.class,
            true,
            true,
            true,
            true,
            false,
            CreatureCategory.DINOSAUR,
            EnergyRechargeCategory.FAST,
            CreatureDiet.CARNIVORE,
            LevelupRate.FAST,
            new Behavior[]{Behavior.DOCILE, Behavior.AGGRESSIVE, Behavior.HERDER, Behavior.BLOCK_BREAKER},
            5855577,
            10439936,
            180,
            0.5f,
            EggTemperature.COLD
    ),
    APATOSAURUS(
            Apatosaurus.class,
            ApatosaurusConfig.class,
            true,
            false,
            true,
            true,
            true,
            CreatureCategory.DINOSAUR,
            EnergyRechargeCategory.SLOW,
            CreatureDiet.HERBIVORE,
            LevelupRate.VERY_SLOW,
            new Behavior[]{Behavior.DOCILE, Behavior.HERDER, Behavior.BLOCK_BREAKER},
            3160621,
            16748800,
            450,
            1,
            EggTemperature.VERY_WARM
    ),
    PARASAUROLOPHUS(
            Parasaurolophus.class,
            ParasaurolophusConfig.class,
            true,
            true,
            true,
            true,
            false,
            CreatureCategory.DINOSAUR,
            EnergyRechargeCategory.FAST,
            CreatureDiet.HERBIVORE,
            LevelupRate.FAST,
            new Behavior[]{Behavior.DOCILE, Behavior.HERDER, Behavior.BLOCK_BREAKER},
            10055190,
            8920579,
            300,
            1,
            EggTemperature.COLD
    ),
    DIMETRODON(
            Dimetrodon.class,
            DimetrodonConfig.class,
            true,
            true,
            true,
            false,
            false,
            CreatureCategory.MAMMAL,
            EnergyRechargeCategory.SLOW,
            CreatureDiet.CARNIVORE,
            LevelupRate.NORMAL,
            new Behavior[]{Behavior.DOCILE, Behavior.AGGRESSIVE, Behavior.BLOCK_BREAKER},
            10968581,
            13198105,
            90,
            0.5f,
            EggTemperature.NEUTRAL
    ),
    COELACANTH(
            Coelacanth.class,
            CoelacanthConfig.class,
            false,
            false,
            false,
            false,
            false,
            CreatureCategory.FISH,
            EnergyRechargeCategory.FAST,
            CreatureDiet.INSECTIVORE,
            null,
            new Behavior[]{Behavior.HERDER},
            1329530,
            1857680,
            0,
            0,
            null
    ),
    MEGAPIRANHA(
            Megapiranha.class,
            MegapiranhaConfig.class,
            false,
            false,
            false,
            false,
            false,
            CreatureCategory.FISH,
            EnergyRechargeCategory.FAST,
            CreatureDiet.CARNIVORE,
            null,
            new Behavior[]{Behavior.DOCILE, Behavior.AGGRESSIVE, Behavior.HERDER},
            8421504,
            10226700,
            0,
            0,
            null
    ),
    SARCOSUCHUS(
            Sarcosuchus.class,
            SarcosuchusConfig.class,
            true,
            true,
            true,
            true,
            false,
            CreatureCategory.REPTILE,
            EnergyRechargeCategory.SLOW,
            CreatureDiet.CARNIVORE,
            LevelupRate.NORMAL,
            new Behavior[]{Behavior.DOCILE, Behavior.AGGRESSIVE, Behavior.BLOCK_BREAKER},
            2302246,
            2627379,
            300,
            0.5f,
            EggTemperature.COLD
    ),
    ANOMALOCARIS(
            Anomalocaris.class,
            AnomalocarisConfig.class,
            true,
            true,
            true,
            true,
            false,
            CreatureCategory.INVERTEBRATE,
            EnergyRechargeCategory.FAST,
            CreatureDiet.CARNIVORE,
            LevelupRate.NORMAL,
            new Behavior[]{Behavior.DOCILE, Behavior.AGGRESSIVE, Behavior.BLOCK_BREAKER},
            10892050,
            12270358,
            300,
            1f,
            null
    ),
    SAUROPHAGANAX(
            Saurophaganax.class,
            SaurophaganaxConfig.class,
            true,
            true,
            true,
            true,
            false,
            CreatureCategory.DINOSAUR,
            EnergyRechargeCategory.FAST,
            CreatureDiet.INSECTIVORE,
            LevelupRate.NORMAL,
            new Behavior[]{Behavior.DOCILE, Behavior.AGGRESSIVE, Behavior.BLOCK_BREAKER, Behavior.NOCTURNAL},
            986895,
            16737280,
            450,
            1f,
            EggTemperature.COLD
    ),
    DIREWOLF(
            Direwolf.class,
            DirewolfConfig.class,
            true,
            true,
            true,
            true,
            false,
            CreatureCategory.MAMMAL,
            EnergyRechargeCategory.SLOW,
            CreatureDiet.CARNIVORE,
            LevelupRate.FAST,
            new Behavior[]{Behavior.DOCILE, Behavior.AGGRESSIVE, Behavior.BLOCK_BREAKER, Behavior.HERDER},
            8421504,
            10066329,
            0,
            0,
            null
    ),
    MEGALOCEROS(
            Megaloceros.class,
            MegalocerosConfig.class,
            true,
            true,
            true,
            true,
            false,
            CreatureCategory.MAMMAL,
            EnergyRechargeCategory.FAST,
            CreatureDiet.HERBIVORE,
            LevelupRate.FAST,
            new Behavior[]{Behavior.DOCILE, Behavior.HERDER, Behavior.BLOCK_BREAKER},
            6048296,
            4666924,
            0,
            0,
            null
    ),
    BARYONYX(
            Baryonyx.class,
            BaryonyxConfig.class,
            true,
            true,
            true,
            true,
            false,
            CreatureCategory.DINOSAUR,
            EnergyRechargeCategory.SLOW,
            CreatureDiet.PISCIVORE,
            LevelupRate.NORMAL,
            new Behavior[]{Behavior.DOCILE, Behavior.AGGRESSIVE, Behavior.BLOCK_BREAKER},
            1277213,
            4674683,
            300,
            1f,
            EggTemperature.COLD
    ),
    PALAEOCASTOR(
            Palaeocastor.class,
            PalaeocastorConfig.class,
            true,
            true,
            true,
            false,
            false,
            CreatureCategory.MAMMAL,
            EnergyRechargeCategory.FAST,
            CreatureDiet.SAXUMAVORE,
            LevelupRate.FAST,
            new Behavior[]{Behavior.DOCILE, Behavior.BLOCK_BREAKER},
            3881787,
            855309,
            0,
            0,
            null
    ),
    ANKYLOSAURUS(
            Ankylosaurus.class,
            AnkylosaurusConfig.class,
            true,
            true,
            true,
            true,
            false,
            CreatureCategory.DINOSAUR,
            EnergyRechargeCategory.SLOW,
            CreatureDiet.HERBIVORE,
            LevelupRate.SLOW,
            new Behavior[]{Behavior.DOCILE, Behavior.HERDER, Behavior.BLOCK_BREAKER},
            4338984,
            4343887,
            300,
            1,
            EggTemperature.COLD
    ),
    DILOPHOSAURUS(
            Dilophosaurus.class,
            DilophosaurusConfig.class,
            true,
            true,
            true,
            true,
            false,
            CreatureCategory.DINOSAUR,
            EnergyRechargeCategory.SLOW,
            CreatureDiet.CARNIVORE,
            LevelupRate.FAST,
            new Behavior[]{Behavior.DOCILE, Behavior.AGGRESSIVE, Behavior.BLOCK_BREAKER},
            16239896,
            1141548,
            180,
            0.5f,
            EggTemperature.COLD
    ),
    GALLIMIMUS(
            Gallimimus.class,
            GallimimusConfig.class,
            true,
            true,
            true,
            true,
            false,
            CreatureCategory.DINOSAUR,
            EnergyRechargeCategory.SLOW,
            CreatureDiet.OMNIVORE,
            LevelupRate.NORMAL,
            new Behavior[]{Behavior.DOCILE, Behavior.HERDER, Behavior.SKITTISH},
            6045223,
            9403247,
            300,
            0.5f,
            EggTemperature.COLD
    );

    private final Class<? extends RiftCreature> creature;
    private final Class<? extends RiftCreatureConfig> config;
    public final boolean isTameable;
    public final boolean isTameableByFeeding;
    public final boolean isBreedable;
    public final boolean canBeSaddled;
    public final boolean canHoldLargeWeapon;
    private final CreatureCategory creatureCategory;
    private final EnergyRechargeCategory energyRechargeCategory;
    private final CreatureDiet creatureDiet;
    private final LevelupRate levelupRate;
    private final Behavior[] behaviors;
    private final int eggPrimary;
    private final int eggSecondary;
    private final int hatchTime; //in seconds
    private final float eggScale;
    private final EggTemperature eggTemperature;
    public Item eggItem;
    public Item sacItem;
    public final String friendlyName;

    RiftCreatureType(Class<? extends RiftCreature> creature, Class<? extends RiftCreatureConfig> config, boolean isTameable, boolean isTameableByFeeding, boolean isBreedable, boolean canBeSaddled, boolean canHoldLargeWeapon, CreatureCategory creatureCategory, EnergyRechargeCategory energyRechargeCategory, CreatureDiet creatureDiet, LevelupRate levelupRate, Behavior[] behaviors, int eggPrimary, int eggSecondary, int hatchTime, float eggScale, EggTemperature eggTemperature) {
        this.creature = creature;
        this.config = config;
        this.isTameable = isTameable;
        this.isTameableByFeeding = isTameableByFeeding;
        this.isBreedable = isBreedable;
        this.canBeSaddled = canBeSaddled;
        this.canHoldLargeWeapon = canHoldLargeWeapon;
        this.creatureCategory = creatureCategory;
        this.energyRechargeCategory = energyRechargeCategory;
        this.creatureDiet = creatureDiet;
        this.levelupRate = levelupRate;
        this.behaviors = behaviors;
        this.friendlyName = this.name().toUpperCase(Locale.ENGLISH).substring(0, 1) + this.name().toLowerCase().substring(1);
        this.eggPrimary = eggPrimary;
        this.eggSecondary = eggSecondary;
        this.hatchTime = hatchTime;
        this.eggScale = eggScale;
        this.eggTemperature = eggTemperature;
    }

    public Class<? extends RiftCreature> getCreature() {
        return this.creature;
    }

    public Class<? extends RiftCreatureConfig> getConfig() {
        return this.config;
    }

    public int gearSlotCount() {
        return (this.canBeSaddled ? 1 : 0) + (this.canHoldLargeWeapon ? 1 : 0);
    }

    public int slotIndexForGear(InventoryGearType gearType) {
        if (this.canBeSaddled && this.canHoldLargeWeapon) {
            if (gearType == InventoryGearType.SADDLE) return 0;
            else if (gearType == InventoryGearType.LARGE_WEAPON) return 1;
            else return -1;
        }
        else if (this.canBeSaddled && !this.canHoldLargeWeapon) {
            if (gearType == InventoryGearType.SADDLE) return 0;
            else return -1;
        }
        return -1;
    }

    public CreatureCategory getCreatureCategory() {
        return this.creatureCategory;
    }

    public CreatureDiet getCreatureDiet() {
        return this.creatureDiet;
    }

    public LevelupRate getLevelupRate() {
        return this.levelupRate;
    }

    public List<Behavior> getBehaviors() {
        return Arrays.asList(this.behaviors);
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

    public EggTemperature getEggTemperature() {
        return this.eggTemperature;
    }

    public int energyRechargeSpeed() { //returns value in ticks
        if (this.energyRechargeCategory == EnergyRechargeCategory.FAST) return 20;
        else return 100;
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

    public String getIdentifier() {
        return "prift:"+this.name().toLowerCase();
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

    public enum InventoryGearType {
        SADDLE,
        LARGE_WEAPON
    }

    public enum EnergyRechargeCategory {
        FAST,
        SLOW;
    }

    public enum CreatureCategory {
        ALL,
        DINOSAUR,
        MAMMAL,
        REPTILE,
        BIRD,
        FISH,
        INVERTEBRATE;

        public String getTranslatedName(boolean plural) {
            String pluralAdd = (plural && !this.equals(ALL)) ? "_plural" : "";
            return I18n.format("type.creature."+this.name().toLowerCase()+pluralAdd);
        }

        public static CreatureCategory safeValOf(String string) {
            for (CreatureCategory category : CreatureCategory.values()) {
                if (category.name().equalsIgnoreCase(string)) {
                    return category;
                }
            }
            return null;
        }
    }

    public enum CreatureDiet {
        HERBIVORE,
        FUNGIVORE,
        CARNIVORE,
        PISCIVORE,
        INSECTIVORE,
        OMNIVORE,
        SAXUMAVORE;
    }

    public enum LevelupRate {
        VERY_SLOW(1.6D),
        SLOW(1.4D),
        NORMAL(1.2D),
        FAST(1D),
        VERY_FAST(0.8D);

        private final double rate;

        LevelupRate(double rate) {
            this.rate = rate;
        }

        public double getRate() {
            return this.rate;
        }
    }

    //these influence wild only behaviors
    public enum Behavior {
        HERDER, //will use herding or pack hunting behaviors
        DOCILE, //will retaliate when attacked
        SKITTISH, //will run from targets
        AGGRESSIVE, //will attack targets
        BLOCK_BREAKER, //will break blocks in front when pursuing a target
        NOCTURNAL //will be active at night and sleep at day
    }
}
