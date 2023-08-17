package anightdazingzoroark.rift.server.items;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.client.creativetab.RiftCreativeTabs;
import anightdazingzoroark.rift.server.entity.CreatureCategory;
import anightdazingzoroark.rift.server.entity.RiftCreatureType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RiftItems {
    public static final List<Item> ITEMS = new ArrayList<>();
    public static final HashMap<String, Item> items = new HashMap<String, Item>();

    public static Item RAW_EXOTIC_MEAT;
    public static Item COOKED_EXOTIC_MEAT;

    public static Item TYRANNOSAURUS_ARM;

    public static void registerItems() {
        RAW_EXOTIC_MEAT = riftFoodItem("raw_exotic_meat", 4, 0.3f, true);
        COOKED_EXOTIC_MEAT = riftFoodItem("cooked_exotic_meat", 8, 0.6f, true);

        TYRANNOSAURUS_ARM = riftGenericItem("tyrannosaurus_arm");

        for (int x = 0; x < RiftCreatureType.values().length; x++) {
            RiftCreatureType creature = RiftCreatureType.values()[x];
            if (creature.getCreatureCategory().equals(CreatureCategory.DINOSAUR) || creature.getCreatureCategory().equals(CreatureCategory.REPTILE) || creature.getCreatureCategory().equals(CreatureCategory.BIRD)) {
                riftEggItem(creature.name().toLowerCase()+"_egg", creature);
            }
        }
    }

    public static Item riftFoodItem(String registryName, int amount, float saturation, boolean isWolfFood) {
        final Item item = new ItemFood(amount, saturation, isWolfFood);
        ITEMS.add(item);
        return registerItem(item, registryName);
    }

    public static Item riftGenericItem(String registryName) {
        final Item item = new Item();
        ITEMS.add(item);
        return registerItem(item, registryName);
    }

    public static Item riftEggItem(String registryName, RiftCreatureType creature) {
        final Item item = new RiftEggItem(creature);
        ITEMS.add(item);
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
