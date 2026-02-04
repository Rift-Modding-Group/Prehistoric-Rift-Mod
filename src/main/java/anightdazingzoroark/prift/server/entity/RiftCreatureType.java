package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.config.*;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.*;
import anightdazingzoroark.prift.server.enums.*;
import anightdazingzoroark.prift.server.items.RiftItems;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
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
            new InventoryGearType[]{InventoryGearType.SADDLE},
            CreatureCategory.DINOSAUR,
            EnergyRechargeCategory.SLOW,
            CreatureDiet.CARNIVORE,
            LevelupRate.SLOW,
            new Behavior[]{Behavior.DOCILE, Behavior.AGGRESSIVE, Behavior.AGGRESSIVE_TO_HUMANS, Behavior.BLOCK_BREAKER},
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
            new InventoryGearType[]{InventoryGearType.SADDLE},
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
            new InventoryGearType[]{},
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
            new InventoryGearType[]{InventoryGearType.SADDLE},
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
            new InventoryGearType[]{InventoryGearType.SADDLE},
            CreatureCategory.DINOSAUR,
            EnergyRechargeCategory.FAST,
            CreatureDiet.CARNIVORE,
            LevelupRate.FAST,
            new Behavior[]{Behavior.DOCILE, Behavior.AGGRESSIVE, Behavior.AGGRESSIVE_TO_HUMANS, Behavior.HERDER, Behavior.BLOCK_BREAKER},
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
            new InventoryGearType[]{InventoryGearType.SADDLE, InventoryGearType.LARGE_WEAPON},
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
            new InventoryGearType[]{InventoryGearType.SADDLE},
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
            new InventoryGearType[]{},
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
            new InventoryGearType[]{},
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
            new InventoryGearType[]{},
            CreatureCategory.FISH,
            EnergyRechargeCategory.FAST,
            CreatureDiet.CARNIVORE,
            null,
            new Behavior[]{Behavior.DOCILE, Behavior.AGGRESSIVE, Behavior.AGGRESSIVE_TO_HUMANS, Behavior.HERDER},
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
            new InventoryGearType[]{InventoryGearType.SADDLE},
            CreatureCategory.REPTILE,
            EnergyRechargeCategory.SLOW,
            CreatureDiet.CARNIVORE,
            LevelupRate.NORMAL,
            new Behavior[]{Behavior.DOCILE, Behavior.AGGRESSIVE, Behavior.AGGRESSIVE_TO_HUMANS, Behavior.BLOCK_BREAKER},
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
            new InventoryGearType[]{InventoryGearType.SADDLE},
            CreatureCategory.INVERTEBRATE,
            EnergyRechargeCategory.FAST,
            CreatureDiet.CARNIVORE,
            LevelupRate.NORMAL,
            new Behavior[]{Behavior.DOCILE, Behavior.AGGRESSIVE, Behavior.AGGRESSIVE_TO_HUMANS, Behavior.BLOCK_BREAKER},
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
            new InventoryGearType[]{InventoryGearType.SADDLE},
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
            new InventoryGearType[]{InventoryGearType.SADDLE},
            CreatureCategory.MAMMAL,
            EnergyRechargeCategory.SLOW,
            CreatureDiet.CARNIVORE,
            LevelupRate.FAST,
            new Behavior[]{Behavior.DOCILE, Behavior.AGGRESSIVE, Behavior.AGGRESSIVE_TO_HUMANS, Behavior.BLOCK_BREAKER, Behavior.HERDER},
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
            new InventoryGearType[]{InventoryGearType.SADDLE},
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
            new InventoryGearType[]{InventoryGearType.SADDLE},
            CreatureCategory.DINOSAUR,
            EnergyRechargeCategory.SLOW,
            CreatureDiet.PISCIVORE,
            LevelupRate.NORMAL,
            new Behavior[]{Behavior.DOCILE, Behavior.AGGRESSIVE, Behavior.AGGRESSIVE_TO_HUMANS, Behavior.BLOCK_BREAKER},
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
            new InventoryGearType[]{},
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
            new InventoryGearType[]{InventoryGearType.SADDLE},
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
            new InventoryGearType[]{InventoryGearType.SADDLE},
            CreatureCategory.DINOSAUR,
            EnergyRechargeCategory.SLOW,
            CreatureDiet.CARNIVORE,
            LevelupRate.FAST,
            new Behavior[]{Behavior.DOCILE, Behavior.AGGRESSIVE, Behavior.AGGRESSIVE_TO_HUMANS, Behavior.BLOCK_BREAKER},
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
            new InventoryGearType[]{InventoryGearType.SADDLE},
            CreatureCategory.DINOSAUR,
            EnergyRechargeCategory.SLOW,
            CreatureDiet.OMNIVORE,
            LevelupRate.NORMAL,
            new Behavior[]{Behavior.DOCILE, Behavior.HERDER},
            6045223,
            9403247,
            300,
            0.5f,
            EggTemperature.COLD
    ),
    TENONTOSAURUS(
            Tenontosaurus.class,
            TenontosaurusConfig.class,
            true,
            true,
            true,
            new InventoryGearType[]{InventoryGearType.SADDLE},
            CreatureCategory.DINOSAUR,
            EnergyRechargeCategory.SLOW,
            CreatureDiet.HERBIVORE,
            LevelupRate.SLOW,
            new Behavior[]{Behavior.DOCILE, Behavior.BLOCK_BREAKER},
            0x8f6f42,
            0x0f0f0f,
            180,
            1f,
            EggTemperature.WARM
    );

    private final Class<? extends RiftCreature> creature;
    private final Class<? extends RiftCreatureConfig> config;
    public final boolean isTameable;
    public final boolean isTameableByFeeding;
    public final boolean isBreedable;
    public final InventoryGearType[] usableGear;
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

    RiftCreatureType(Class<? extends RiftCreature> creature, Class<? extends RiftCreatureConfig> config, boolean isTameable, boolean isTameableByFeeding, boolean isBreedable, InventoryGearType[] usableGear, CreatureCategory creatureCategory, EnergyRechargeCategory energyRechargeCategory, CreatureDiet creatureDiet, LevelupRate levelupRate, Behavior[] behaviors, int eggPrimary, int eggSecondary, int hatchTime, float eggScale, EggTemperature eggTemperature) {
        this.creature = creature;
        this.config = config;
        this.isTameable = isTameable;
        this.isTameableByFeeding = isTameableByFeeding;
        this.isBreedable = isBreedable;
        this.usableGear = usableGear;
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
        return this.usableGear.length;
    }

    public boolean canUseGearType(InventoryGearType gearTypeToSearch) {
        for (InventoryGearType gearType : this.usableGear) {
            if (gearType == gearTypeToSearch) return true;
        }
        return false;
    }

    public int slotIndexForGear(InventoryGearType gearTypeToSearch) {
        if (this.usableGear.length > 0) {
            for (int i = 0; i < this.usableGear.length; i++) {
                InventoryGearType gearType = this.usableGear[i];
                if (gearType == gearTypeToSearch) return i;
            }
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

    public boolean isFavoriteFood(ItemStack stack) {
        RiftCreatureConfig creatureConfig = RiftConfigHandler.getConfig(this);
        if (creatureConfig != null) {
            List<RiftCreatureConfig.Food> favoriteFood = creatureConfig.general.favoriteFood;
            for (RiftCreatureConfig.Food food : favoriteFood) {
                if (RiftUtil.itemStackEqualToString(stack, food.itemId)) return true;
            }
        }
        return false;
    }

    public int getFavoriteFoodHeal(ItemStack stack, double maxHealth) {
        RiftCreatureConfig creatureConfig = RiftConfigHandler.getConfig(this);
        if (creatureConfig != null) {
            List<RiftCreatureConfig.Food> favoriteFood = creatureConfig.general.favoriteFood;
            for (RiftCreatureConfig.Food food : favoriteFood) {
                if (RiftUtil.itemStackEqualToString(stack, food.itemId)) {
                    return (int)Math.ceil(maxHealth * food.percentageHeal);
                }
            }
        }
        return 0;
    }

    public boolean isEnergyRegenItem(ItemStack stack) {
        List<String> itemList = new ArrayList<>();
        if (this.getCreatureDiet() == RiftCreatureType.CreatureDiet.HERBIVORE || this.getCreatureDiet() == RiftCreatureType.CreatureDiet.FUNGIVORE) itemList = Arrays.asList(GeneralConfig.herbivoreRegenEnergyFoods);
        else if (this.getCreatureDiet() == RiftCreatureType.CreatureDiet.CARNIVORE || this.getCreatureDiet() == RiftCreatureType.CreatureDiet.PISCIVORE || this.getCreatureDiet() == RiftCreatureType.CreatureDiet.INSECTIVORE) itemList = Arrays.asList(GeneralConfig.carnivoreRegenEnergyFoods);
        else if (this.getCreatureDiet() == RiftCreatureType.CreatureDiet.OMNIVORE) {
            itemList = new ArrayList<>(Arrays.asList(GeneralConfig.herbivoreRegenEnergyFoods));
            itemList.addAll(Arrays.asList(GeneralConfig.carnivoreRegenEnergyFoods));
        }

        for (String foodItem : itemList) {
            int first = foodItem.indexOf(":");
            int second = foodItem.indexOf(":", first + 1);
            int third = foodItem.indexOf(":", second + 1);
            String itemId = foodItem.substring(0, second);
            int itemData = Integer.parseInt(foodItem.substring(second + 1, third));
            if (!stack.isEmpty() && stack.getItem().equals(Item.getByNameOrId(itemId)) && (itemData == -1 || itemData == stack.getMetadata())) return true;
        }

        return false;
    }

    public int getEnergyRegenItemValue(ItemStack stack) {
        List<String> itemList = new ArrayList<>();
        if (this.getCreatureDiet() == RiftCreatureType.CreatureDiet.HERBIVORE || this.getCreatureDiet() == RiftCreatureType.CreatureDiet.FUNGIVORE) itemList = Arrays.asList(GeneralConfig.herbivoreRegenEnergyFoods);
        else if (this.getCreatureDiet() == RiftCreatureType.CreatureDiet.CARNIVORE || this.getCreatureDiet() == RiftCreatureType.CreatureDiet.PISCIVORE || this.getCreatureDiet() == RiftCreatureType.CreatureDiet.INSECTIVORE) itemList = Arrays.asList(GeneralConfig.carnivoreRegenEnergyFoods);
        else if (this.getCreatureDiet() == RiftCreatureType.CreatureDiet.OMNIVORE) {
            itemList = new ArrayList<>(Arrays.asList(GeneralConfig.herbivoreRegenEnergyFoods));
            itemList.addAll(Arrays.asList(GeneralConfig.carnivoreRegenEnergyFoods));
        }

        for (String itemEntry : itemList) {
            int first = itemEntry.indexOf(":");
            int second = itemEntry.indexOf(":", first + 1);
            int third = itemEntry.indexOf(":", second + 1);
            String itemId = itemEntry.substring(0, second);
            int itemData = Integer.parseInt(itemEntry.substring(second + 1, third));
            if (stack.getItem().equals(Item.getByNameOrId(itemId)) && (itemData == -1 || itemData == stack.getMetadata())) {
                return Integer.parseInt(itemEntry.substring(third + 1));
            }
        }
        return 0;
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

    public int getMaxXP(int level) {
        return (int)Math.round((double)level * this.levelupRate.getRate() * 25D);
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

        public String getTranslatedName() {
            return I18n.format("diet.creature."+this.name().toLowerCase());
        }
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

        public String getTranslatedName() {
            return I18n.format("levelup_rate.creature."+this.name().toLowerCase());
        }
    }

    //these mostly influence wild only behaviors
    public enum Behavior {
        HERDER, //will use herding or pack hunting behaviors
        DOCILE, //will retaliate when attacked
        AGGRESSIVE, //will attack targets
        AGGRESSIVE_TO_HUMANS, //will attack humans
        BLOCK_BREAKER, //will break blocks in front when pursuing a target
        NOCTURNAL //will be active at night and sleep at day
    }
}
