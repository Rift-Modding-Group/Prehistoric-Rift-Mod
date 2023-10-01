package anightdazingzoroark.rift.server.items;

import anightdazingzoroark.rift.client.creativetab.RiftCreativeTabs;
import anightdazingzoroark.rift.server.entity.RiftCreatureType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static anightdazingzoroark.rift.server.entity.RiftCreatureType.registerEggs;

public class RiftItems {
    public static final List<Item> ITEMS = new ArrayList<>();
    public static final HashMap<String, Item> items = new HashMap<String, Item>();

    public static Item RAW_EXOTIC_MEAT;
    public static Item COOKED_EXOTIC_MEAT;
    public static Item RAW_FIBROUS_MEAT;
    public static Item COOKED_FIBROUS_MEAT;
    public static Item FIBER_BAR;

    public static Item TYRANNOSAURUS_ARM;
    public static Item STEGOSAURUS_PLATE;

    public static void registerItems() {
        RAW_EXOTIC_MEAT = riftFoodItem("raw_exotic_meat", 4, 0.3f, true);
        COOKED_EXOTIC_MEAT = riftFoodItem("cooked_exotic_meat", 8, 0.6f, true);
        RAW_FIBROUS_MEAT = riftFoodItem("raw_fibrous_meat", 1, 0.45f, true);
        COOKED_FIBROUS_MEAT = riftFibrousFoodItem("cooked_fibrous_meat", 2, true);

        FIBER_BAR = riftFibrousFoodItem("fiber_bar", 2, false);

        TYRANNOSAURUS_ARM = riftGenericItem("tyrannosaurus_arm");
        STEGOSAURUS_PLATE = riftGenericItem("stegosaurus_plate");

        registerEggs();
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

    public static Item riftGenericItem(String registryName) {
        final Item item = new Item();
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
