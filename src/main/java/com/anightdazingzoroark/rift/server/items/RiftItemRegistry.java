package com.anightdazingzoroark.rift.server.items;

import com.anightdazingzoroark.rift.RiftInitialize;
import com.anightdazingzoroark.rift.server.entities.RiftEntityRegistry;
import com.anightdazingzoroark.rift.server.entities.creatures.TyrannosaurusEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RiftItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RiftInitialize.MODID);

    //treats
    public static final RegistryObject<Item> BERRY_TREAT = ITEMS.register("berry_treat",
            () -> new TreatItem(new Item.Properties().tab(RiftInitialize.RIFT_ITEMS_TAB)
                    .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.2f).build())));
    public static final RegistryObject<Item> GRASSY_TREAT = ITEMS.register("grassy_treat",
            () -> new TreatItem(new Item.Properties().tab(RiftInitialize.RIFT_ITEMS_TAB)
                    .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.2f).build())));
    public static final RegistryObject<Item> ROOTED_TREAT = ITEMS.register("rooted_treat",
            () -> new TreatItem(new Item.Properties().tab(RiftInitialize.RIFT_ITEMS_TAB)
                    .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.2f).build())));
    public static final RegistryObject<Item> LEAFY_TREAT = ITEMS.register("leafy_treat",
            () -> new TreatItem(new Item.Properties().tab(RiftInitialize.RIFT_ITEMS_TAB)
                    .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.2f).build())));
    public static final RegistryObject<Item> FLESH_TREAT = ITEMS.register("flesh_treat",
            () -> new TreatItem(new Item.Properties().tab(RiftInitialize.RIFT_ITEMS_TAB)
                    .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.2f).build())));
    public static final RegistryObject<Item> FISH_TREAT = ITEMS.register("fish_treat",
            () -> new TreatItem(new Item.Properties().tab(RiftInitialize.RIFT_ITEMS_TAB)
                    .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.2f).build())));
    public static final RegistryObject<Item> CARRION_TREAT = ITEMS.register("carrion_treat",
            () -> new TreatItem(new Item.Properties().tab(RiftInitialize.RIFT_ITEMS_TAB)
                    .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.2f).build())));
    public static final RegistryObject<Item> BUG_TREAT = ITEMS.register("bug_treat",
            () -> new TreatItem(new Item.Properties().tab(RiftInitialize.RIFT_ITEMS_TAB)
                    .food(new FoodProperties.Builder().nutrition(2).saturationMod(0.2f).build())));

    //meat related stuff
    public static final RegistryObject<Item> RAW_EXOTIC_MEAT = ITEMS.register("raw_exotic_meat",
            () -> new Item(new Item.Properties().tab(RiftInitialize.RIFT_ITEMS_TAB)
                    .food(new FoodProperties.Builder().nutrition(4).saturationMod(0.6f).build())));

    public static final RegistryObject<Item> COOKED_EXOTIC_MEAT = ITEMS.register("cooked_exotic_meat",
            () -> new Item(new Item.Properties().tab(RiftInitialize.RIFT_ITEMS_TAB)
                    .food(new FoodProperties.Builder().nutrition(8).saturationMod(1.6f).build())));

    public static final RegistryObject<Item> RAW_HEMOLYMPH = ITEMS.register("raw_hemolymph",
            () -> new Item(new Item.Properties().tab(RiftInitialize.RIFT_ITEMS_TAB)
                    .food(new FoodProperties.Builder().nutrition(1).saturationMod(0.6f).effect(
                            () -> new MobEffectInstance(MobEffects.POISON, 600, 0),
            1F).build())));

    public static final RegistryObject<Item> BOILED_HEMOLYMPH = ITEMS.register("boiled_hemolymph",
            () -> new Item(new Item.Properties().tab(RiftInitialize.RIFT_ITEMS_TAB)
                    .food(new FoodProperties.Builder().nutrition(6).saturationMod(1.6f).build())));

    //tribute items
    public static final RegistryObject<Item> TYRANNOSAURUS_ARM = ITEMS.register("tyrannosaurus_arm",
            () -> new Item(new Item.Properties().tab(RiftInitialize.RIFT_ITEMS_TAB)));

    //spawn eggs
    public static final RegistryObject<Item> TYRANNOSAURUS_SPAWN_EGG = ITEMS.register("tyrannosaurus_spawn_egg",
            () -> new ForgeSpawnEggItem(RiftEntityRegistry.TYRANNOSAURUS,3670016, 2428687,
                    new Item.Properties().tab(RiftInitialize.RIFT_ENTITIES_TAB)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
