package anightdazingzoroark.prift.server.item;

import anightdazingzoroark.prift.client.RiftCreativeTabs;
import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureRegistry;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RiftItems {
    public static final List<Item> ITEMS = new ArrayList<>();
    private static final Map<String, Item> TRIBUTE_ITEMS = new LinkedHashMap<>();
    private static boolean itemsRegistered;
    private static boolean recipesRegistered;

    public static Item CREATURE_SPAWN_EGG;
    public static Item RAW_EXOTIC_MEAT;
    public static Item COOKED_EXOTIC_MEAT;
    public static Item CREATIVE_MEAL;

    public static Item getTributeItem(@NotNull String creatureName) {
        return TRIBUTE_ITEMS.get(creatureName);
    }

    //-----registry stuff-----
    public static void registerItems() {
        if (itemsRegistered) throw new IllegalStateException("Items have already been registered!");

        CREATURE_SPAWN_EGG = registerItem(new RiftCreatureSpawnEggItem(), "creature_spawn_egg", false);
        RAW_EXOTIC_MEAT = registerItem(new ItemFood(3, 0.3f, true), "raw_exotic_meat", true);
        COOKED_EXOTIC_MEAT = registerItem(new ItemFood(6, 0.3f, true), "cooked_exotic_meat", true);
        CREATIVE_MEAL = registerItem(new Item() {
            @Override
            @SideOnly(Side.CLIENT)
            public boolean hasEffect(ItemStack stack) {
                return true;
            }

            @Override
            @SideOnly(Side.CLIENT)
            public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag tooltipFlag) {
                tooltip.add(TextFormatting.GRAY + I18n.format("item.creative_meal.tooltip"));
            }
        }, "creative_meal", true);

        for (Map.Entry<String, RiftCreatureBuilder> creatureEntry : RiftCreatureRegistry.getCreatureBuilders().entrySet()) {
            String creatureName = creatureEntry.getKey();
            String tributeItemName = creatureName + "_" + creatureEntry.getValue().getTributeItemPartName();
            TRIBUTE_ITEMS.put(creatureName, registerItem(new Item(), tributeItemName, true));
        }
        itemsRegistered = true;
    }

    /*
    public static void registerOreDictionaryTags() {

    }
     */

    /**
     * waitin for that json furnace recipe proposal for cleanroom to be added...
     * */
    public static void registerFurnaceRecipes() {
        if (recipesRegistered) throw new IllegalStateException("Recipes have already been registered");

        GameRegistry.addSmelting(RiftItems.RAW_EXOTIC_MEAT, new ItemStack(RiftItems.COOKED_EXOTIC_MEAT), 0.35f);
        recipesRegistered = true;
    }

    private static Item registerItem(Item item, String registryName, boolean canBeInCreative) {
        if (canBeInCreative) item.setCreativeTab(RiftCreativeTabs.creativeItemsTab);
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
