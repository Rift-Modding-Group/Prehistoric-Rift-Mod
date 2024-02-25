package anightdazingzoroark.prift.server.items;

import anightdazingzoroark.prift.client.creativetab.RiftCreativeTabs;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.projectile.RiftProjectiles;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RiftItems {
    public static final List<Item> ITEMS = new ArrayList<>();
    public static final HashMap<String, Item> items = new HashMap<String, Item>();

    public static Item RAW_EXOTIC_MEAT;
    public static Item COOKED_EXOTIC_MEAT;
    public static Item RAW_FIBROUS_MEAT;
    public static Item COOKED_FIBROUS_MEAT;
    public static Item RAW_DODO_MEAT;
    public static Item COOKED_DODO_MEAT;
    public static Item RAW_HADROSAUR_MEAT;
    public static Item COOKED_HADROSAUR_MEAT;
    public static Item RAW_COELACANTH;
    public static Item RAW_MEGAPIRANHA;
    public static Item COOKED_MEGAPIRANHA;
    public static Item TRUFFLE;
    public static Item FIBER_BAR;
    public static Item BASIC_HERBIVORE_MEAL;
    public static Item BASIC_CARNIVORE_MEAL;
    public static Item ADVANCED_HERBIVORE_MEAL;
    public static Item ADVANCED_CARNIVORE_MEAL;
    public static Item CREATIVE_MEAL;
    public static Item FLAME_STIMULANT;
    public static Item EXTREME_FLAME_STIMULANT;
    public static Item FROST_STIMULANT;
    public static Item EXTREME_FROST_STIMULANT;
    public static Item NEUTRAL_STIMULANT;

    public static Item TYRANNOSAURUS_ARM;
    public static Item STEGOSAURUS_PLATE;
    public static Item DODO_BEAK;
    public static Item TRICERATOPS_HORN;
    public static Item UTAHRAPTOR_CLAW;
    public static Item APATOSAURUS_VERTEBRAE;
    public static Item PARASAUROLOPHUS_HORN;
    public static Item DIMETRODON_SAIL;
    public static Item COELACANTH_SCALES;
    public static Item MEGAPIRANHA_SCALES;

    public static Item APATOSAURUS_PLATFORM;
    public static Item CANNON;
    public static Item MORTAR;
    public static Item CATAPULT;

    public static Item CANNONBALL;
    public static Item MORTAR_SHELL;
    public static Item CATAPULT_BOULDER;

    public static Item WRENCH;
    public static Item COMMAND_CONSOLE;

    public static void registerItems() {
        RAW_EXOTIC_MEAT = riftFoodItem("raw_exotic_meat", 4, 0.3f, true);
        COOKED_EXOTIC_MEAT = riftFoodItem("cooked_exotic_meat", 8, 0.6f, true);
        RAW_FIBROUS_MEAT = riftFoodItem("raw_fibrous_meat", 1, 0.45f, true);
        COOKED_FIBROUS_MEAT = riftFibrousFoodItem("cooked_fibrous_meat", 2, true);
        RAW_DODO_MEAT = riftSpecialMeatItem("raw_dodo_meat", 2, 0.3f, new PotionEffect(MobEffects.HUNGER, 200));
        COOKED_DODO_MEAT = riftSpecialMeatItem("cooked_dodo_meat", 4, 0.6f, new PotionEffect(MobEffects.ABSORPTION, 600, 2));
        RAW_HADROSAUR_MEAT = riftSpecialMeatItem("raw_hadrosaur_meat", 3, 0.3f,  new PotionEffect(MobEffects.HUNGER, 200));
        COOKED_HADROSAUR_MEAT = riftSpecialMeatItem("cooked_hadrosaur_meat", 4, 0.6f, new PotionEffect(MobEffects.RESISTANCE, 600, 1));
        RAW_COELACANTH = riftSpecialMeatItem("raw_coelacanth", 1, 0.3f, new PotionEffect(MobEffects.WITHER, 100, 0));
        RAW_MEGAPIRANHA = riftSpecialMeatItem("raw_megapiranha", 2, 0.3f, new PotionEffect(MobEffects.HUNGER, 200));
        COOKED_MEGAPIRANHA = riftSpecialMeatItem("cooked_megapiranha", 4, 0.6f, new PotionEffect(MobEffects.REGENERATION, 300));
        BASIC_HERBIVORE_MEAL = riftGenericItem("basic_herbivore_meal", true);
        BASIC_CARNIVORE_MEAL = riftGenericItem("basic_carnivore_meal", true);
        ADVANCED_HERBIVORE_MEAL = riftGlintedItem("advanced_herbivore_meal", true);
        ADVANCED_CARNIVORE_MEAL = riftGlintedItem("advanced_carnivore_meal", true);
        CREATIVE_MEAL = riftGlintedItem("creative_meal", true);
        FLAME_STIMULANT = riftGenericItem("flame_stimulant", false);
        EXTREME_FLAME_STIMULANT = riftGlintedItem("extreme_flame_stimulant", false);
        FROST_STIMULANT = riftGenericItem("frost_stimulant", false);
        EXTREME_FROST_STIMULANT = riftGlintedItem("extreme_frost_stimulant", false);
        NEUTRAL_STIMULANT = riftGenericItem("neutral_stimulant", false);

        TRUFFLE = riftGenericItem("truffle", true);

        FIBER_BAR = riftFibrousFoodItem("fiber_bar", 2, false);

        TYRANNOSAURUS_ARM = riftGenericItem("tyrannosaurus_arm", true);
        STEGOSAURUS_PLATE = riftGenericItem("stegosaurus_plate", true);
        DODO_BEAK = riftGenericItem("dodo_beak", true);
        TRICERATOPS_HORN = riftGenericItem("triceratops_horn", true);
        UTAHRAPTOR_CLAW = riftGenericItem("utahraptor_claw", true);
        APATOSAURUS_VERTEBRAE = riftGenericItem("apatosaurus_vertebrae", true);
        PARASAUROLOPHUS_HORN = riftGenericItem("parasaurolophus_horn", true);
        DIMETRODON_SAIL = riftGenericItem("dimetrodon_sail", true);
        COELACANTH_SCALES = riftGenericItem("coelacanth_scales", true);
        MEGAPIRANHA_SCALES = riftGenericItem("megapiranha_scales", true);

        APATOSAURUS_PLATFORM = riftGenericItem("apatosaurus_platform", false);
        CANNON = riftLargeWeaponItem("cannon", RiftLargeWeaponType.CANNON);
        MORTAR = riftLargeWeaponItem("mortar", RiftLargeWeaponType.MORTAR);
        CATAPULT = riftLargeWeaponItem("catapult", RiftLargeWeaponType.CATAPULT);

        CANNONBALL = riftGenericItem("cannonball", false);
        MORTAR_SHELL = riftGenericItem("mortar_shell", false);
        CATAPULT_BOULDER = riftGenericItem("catapult_boulder", false);

        WRENCH = riftGenericItem("wrench", false);
        COMMAND_CONSOLE = riftGenericItem("command_console", false);

        RiftCreatureType.registerEggs();

        //dont ask why this is here
        RiftProjectiles.registerProjectiles();
    }

    public static void registerOreDicTags() {
        //for herbivore meal
        OreDictionary.registerOre("herbivoreMealIng", Items.WHEAT);
        OreDictionary.registerOre("herbivoreMealIng", Items.CARROT);
        OreDictionary.registerOre("herbivoreMealIng", Items.POTATO);
        OreDictionary.registerOre("herbivoreMealIng", Items.BEETROOT);
        OreDictionary.registerOre("herbivoreMealIng", Items.APPLE);
        OreDictionary.registerOre("herbivoreMealIng", Blocks.CACTUS);

        //for carnivore meal
        OreDictionary.registerOre("carnivoreMealIng", Items.PORKCHOP);
        OreDictionary.registerOre("carnivoreMealIng", Items.MUTTON);
        OreDictionary.registerOre("carnivoreMealIng", Items.CHICKEN);
        OreDictionary.registerOre("carnivoreMealIng", Items.BEEF);
        OreDictionary.registerOre("carnivoreMealIng", RiftItems.RAW_EXOTIC_MEAT);

        //to other lists
        OreDictionary.registerOre("listAllmeat", RiftItems.RAW_EXOTIC_MEAT);
        OreDictionary.registerOre("listAllmeat", RiftItems.COOKED_EXOTIC_MEAT);
        OreDictionary.registerOre("listAllmeat", RiftItems.RAW_DODO_MEAT);
        OreDictionary.registerOre("listAllmeat", RiftItems.COOKED_DODO_MEAT);
        OreDictionary.registerOre("listAllmeat", RiftItems.RAW_FIBROUS_MEAT);
        OreDictionary.registerOre("listAllmeat", RiftItems.COOKED_FIBROUS_MEAT);
    }

    public static Item riftFoodItem(String registryName, int amount, float saturation, boolean isWolfFood) {
        final Item item = new ItemFood(amount, saturation, isWolfFood);
        return registerItem(item, registryName);
    }

    public static Item riftFibrousFoodItem(String registryName, int amount, boolean isWolfFood) {
        final RiftFibrousFoodItem item = new RiftFibrousFoodItem(amount, isWolfFood);
        return registerItem(item, registryName);
    }

    public static Item riftSpecialMeatItem(String registryName, int amount, float saturation, PotionEffect eatenEffect) {
        final RiftSpecialMeatItem item = new RiftSpecialMeatItem(amount, saturation, eatenEffect);
        item.setAlwaysEdible();
        return registerItem(item, registryName);
    }

    public static Item riftGenericItem(String registryName, boolean stackable) {
        final Item item = new Item();
        if (!stackable) item.setMaxStackSize(1);
        return registerItem(item, registryName);
    }

    public static Item riftGlintedItem(String registryName, boolean stackable) {
        final RiftGlintedItem item = new RiftGlintedItem();
        if (!stackable) item.setMaxStackSize(1);
        return registerItem(item, registryName);
    }

    public static Item riftEggItem(String registryName, RiftCreatureType creature) {
        final Item item = new RiftEggItem(creature);
        return registerItem(item, registryName);
    }

    public static Item riftLargeWeaponItem(String registryName, RiftLargeWeaponType weaponType) {
        final RiftLargeWeaponItem item = new RiftLargeWeaponItem(weaponType);
        return registerItem(item, registryName);
    }

    public static Item registerItem(Item item, String registryName) {
        item.setCreativeTab(RiftCreativeTabs.creativeItemsTab);
        item.setRegistryName(registryName);
        item.setTranslationKey(registryName);
        ITEMS.add(item);
        return item;
    }

    @SubscribeEvent
    public void onItemRegistry(RegistryEvent.Register<Item> e) {
        IForgeRegistry<Item> reg = e.getRegistry();
        reg.registerAll(ITEMS.toArray(new Item[0]));
    }
}
