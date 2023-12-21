package anightdazingzoroark.prift.server.items;

import anightdazingzoroark.prift.client.creativetab.RiftCreativeTabs;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.projectile.RiftProjectiles;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
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
    public static Item FIBER_BAR;
    public static Item BASIC_HERBIVORE_MEAL;
    public static Item BASIC_CARNIVORE_MEAL;
    public static Item ADVANCED_HERBIVORE_MEAL;
    public static Item ADVANCED_CARNIVORE_MEAL;
    public static Item CREATIVE_MEAL;

    public static Item TYRANNOSAURUS_ARM;
    public static Item STEGOSAURUS_PLATE;
    public static Item DODO_BEAK;
    public static Item TRICERATOPS_HORN;
    public static Item UTAHRAPTOR_CLAW;

    public static Item APATOSAURUS_PLATFORM;

    public static void registerItems() {
        RAW_EXOTIC_MEAT = riftFoodItem("raw_exotic_meat", 4, 0.3f, true);
        COOKED_EXOTIC_MEAT = riftFoodItem("cooked_exotic_meat", 8, 0.6f, true);
        RAW_FIBROUS_MEAT = riftFoodItem("raw_fibrous_meat", 1, 0.45f, true);
        COOKED_FIBROUS_MEAT = riftFibrousFoodItem("cooked_fibrous_meat", 2, true);
        RAW_DODO_MEAT = riftDodoMeatItem("raw_dodo_meat", 4, 0.3f, false, true);
        COOKED_DODO_MEAT = riftDodoMeatItem("cooked_dodo_meat", 8, 0.6f, true, true);
        BASIC_HERBIVORE_MEAL = riftGenericItem("basic_herbivore_meal", true);
        BASIC_CARNIVORE_MEAL = riftGenericItem("basic_carnivore_meal", true);
        ADVANCED_HERBIVORE_MEAL = riftGlintedItem("advanced_herbivore_meal");
        ADVANCED_CARNIVORE_MEAL = riftGlintedItem("advanced_carnivore_meal");
        CREATIVE_MEAL = riftGlintedItem("creative_meal");

        FIBER_BAR = riftFibrousFoodItem("fiber_bar", 2, false);

        TYRANNOSAURUS_ARM = riftGenericItem("tyrannosaurus_arm", true);
        STEGOSAURUS_PLATE = riftGenericItem("stegosaurus_plate", true);
        DODO_BEAK = riftGenericItem("dodo_beak", true);
        TRICERATOPS_HORN = riftGenericItem("triceratops_horn", true);
        UTAHRAPTOR_CLAW = riftGenericItem("utahraptor_claw", true);

        APATOSAURUS_PLATFORM = riftGenericItem("apatosaurus_platform", false);

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
    }

    public static Item riftUnstackableItem(String registryName) {
        final Item item = new Item().setMaxStackSize(1);
        return registerItem(item, registryName);
    }

    public static Item riftFoodItem(String registryName, int amount, float saturation, boolean isWolfFood) {
        final Item item = new ItemFood(amount, saturation, isWolfFood);
        return registerItem(item, registryName);
    }

    public static Item riftFibrousFoodItem(String registryName, int amount, boolean isWolfFood) {
        final RiftFibrousFoodItem item = new RiftFibrousFoodItem(amount, isWolfFood);
        return registerItem(item, registryName);
    }

    public static Item riftDodoMeatItem(String registryName, int amount, float saturation, boolean isCooked, boolean isWolfFood) {
        final RiftDodoMeatItem item = new RiftDodoMeatItem(amount, saturation, isCooked, isWolfFood);
        item.setAlwaysEdible();
        return registerItem(item, registryName);
    }

    public static Item riftGenericItem(String registryName, boolean stackable) {
        final Item item = new Item();
        if (!stackable) item.setMaxStackSize(1);
        return registerItem(item, registryName);
    }

    public static Item riftGlintedItem(String registryName) {
        final RiftGlintedItem item = new RiftGlintedItem();
        return registerItem(item, registryName);
    }

    public static Item riftEggItem(String registryName, RiftCreatureType creature) {
        final Item item = new RiftEggItem(creature);
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
