package com.anightdazingzoroark.rift.registry;

import com.anightdazingzoroark.rift.InitializeServer;
import com.anightdazingzoroark.rift.items.FoodItems;
import com.anightdazingzoroark.rift.items.GlintItems;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModItems {
    //blocks
    ///thatch blocks
    public static final Item OAK_THATCH_BLOCK = new BlockItem(ModBlocks.OAK_THATCH_BLOCK, new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item BIRCH_THATCH_BLOCK = new BlockItem(ModBlocks.BIRCH_THATCH_BLOCK, new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item SPRUCE_THATCH_BLOCK = new BlockItem(ModBlocks.SPRUCE_THATCH_BLOCK, new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item JUNGLE_THATCH_BLOCK = new BlockItem(ModBlocks.JUNGLE_THATCH_BLOCK, new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item DARK_OAK_THATCH_BLOCK = new BlockItem(ModBlocks.DARK_OAK_THATCH_BLOCK, new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item ACACIA_THATCH_BLOCK = new BlockItem(ModBlocks.ACACIA_THATCH_BLOCK, new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item CRIMSON_THATCH_BLOCK = new BlockItem(ModBlocks.CRIMSON_THATCH_BLOCK, new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item WARPED_THATCH_BLOCK = new BlockItem(ModBlocks.WARPED_THATCH_BLOCK, new Item.Settings().group(InitializeServer.RIFT_ITEMS));

    //items
    ///tools
    public static final Item COMMAND_STAFF = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).maxCount(1));
    public static final Item HARVEST_STAFF = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).maxCount(1));
    public static final Item COMMAND_CONSOLE = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).maxCount(1));
    public static final Item WRENCH = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).maxCount(1));
    ///stationary weapon ammo
    public static final Item CANNONBALL = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).maxCount(1));
    public static final Item MORTAR_SHELL = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).maxCount(1));
    ///fruit/vegetable/edible fungus items
    public static final Item TRUFFLE = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).food(FoodItems.TRUFFLE));
    ///meat items
    public static final Item RAW_EXOTIC_MEAT = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).food(FoodItems.RAW_EXOTIC_MEAT));
    public static final Item COOKED_EXOTIC_MEAT = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).food(FoodItems.COOKED_EXOTIC_MEAT));
    public static final Item RAW_DODO_MEAT = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).food(FoodItems.RAW_DODO_MEAT));
    public static final Item COOKED_DODO_MEAT = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).food(FoodItems.COOKED_DODO_MEAT));
    public static final Item RAW_HADROSAUR_MEAT = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).food(FoodItems.RAW_HADROSAUR_MEAT));
    public static final Item COOKED_HADROSAUR_MEAT = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).food(FoodItems.COOKED_HADROSAUR_MEAT));
    ///other food items
    public static final Item SMALL_FRIED_EGG = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).food(FoodItems.SMALL_FRIED_EGG));
    public static final Item MEDIUM_FRIED_EGG = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).food(FoodItems.MEDIUM_FRIED_EGG));
    public static final Item LARGE_FRIED_EGG = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).food(FoodItems.LARGE_FRIED_EGG));
    public static final Item MIXED_MEAT = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).food(FoodItems.MIXED_MEAT));
    public static final Item ENCHANTED_MIXED_MEAT = new GlintItems(new Item.Settings().group(InitializeServer.RIFT_ITEMS).food(FoodItems.ENCHANTED_MIXED_MEAT));
    ///treat items
    public static final Item FLESH_TREAT = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).food(FoodItems.TREAT));
    public static final Item FISH_TREAT = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).food(FoodItems.TREAT));
    public static final Item BERRY_TREAT = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).food(FoodItems.TREAT));
    public static final Item ROOTED_TREAT = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).food(FoodItems.TREAT));
    public static final Item GRASSY_TREAT = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).food(FoodItems.TREAT));
    public static final Item LEAFY_TREAT = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS).food(FoodItems.TREAT));
    ///stimulant items
    public static final Item FLAME_STIMULANT = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item FROST_STIMULANT = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item NEUTRAL_STIMULANT = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    ///thatch items
    public static final Item OAK_THATCH = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item BIRCH_THATCH = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item SPRUCE_THATCH = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item JUNGLE_THATCH = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item DARK_OAK_THATCH = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item ACACIA_THATCH = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item CRIMSON_THATCH = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item WARPED_THATCH = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    //other resource items
    public static final Item GRASS_FIBER = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    ///tribute items
    public static final Item STEGOSAURUS_PLATE = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item TYRANNOSAURUS_ARM = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item DODO_BEAK = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item TRICERATOPS_HORN = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item UTAHRAPTOR_CLAW = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item APATOSAURUS_VERTEBRAE = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item PARASAUROLOPHUS_HORN = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS));
    public static final Item DIMETRODON_SAIL = new Item(new Item.Settings().group(InitializeServer.RIFT_ITEMS));

    public static void registerItems() {
        //blocks
        ///thatch blocks
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "oak_thatch_block"), OAK_THATCH_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "birch_thatch_block"), BIRCH_THATCH_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "spruce_thatch_block"), SPRUCE_THATCH_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "jungle_thatch_block"), JUNGLE_THATCH_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "dark_oak_thatch_block"), DARK_OAK_THATCH_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "acacia_thatch_block"), ACACIA_THATCH_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "crimson_thatch_block"), CRIMSON_THATCH_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "warped_thatch_block"), WARPED_THATCH_BLOCK);

        //items
        ///tools
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "command_staff"), COMMAND_STAFF);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "harvest_staff"), HARVEST_STAFF);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "command_console"), COMMAND_CONSOLE);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "wrench"), WRENCH);
        ///stationary weapon ammo
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "cannonball"), CANNONBALL);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "mortar_shell"), MORTAR_SHELL);
        ///fruit/vegetable/edible fungus items
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "truffle"), TRUFFLE);
        ///meat items
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "raw_exotic_meat"), RAW_EXOTIC_MEAT);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "cooked_exotic_meat"), COOKED_EXOTIC_MEAT);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "raw_dodo_meat"), RAW_DODO_MEAT);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "cooked_dodo_meat"), COOKED_DODO_MEAT);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "raw_hadrosaur_meat"), RAW_HADROSAUR_MEAT);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "cooked_hadrosaur_meat"), COOKED_HADROSAUR_MEAT);
        ///other food items
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "small_fried_egg"), SMALL_FRIED_EGG);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "medium_fried_egg"), MEDIUM_FRIED_EGG);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "large_fried_egg"), LARGE_FRIED_EGG);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "mixed_meat"), MIXED_MEAT);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "enchanted_mixed_meat"), ENCHANTED_MIXED_MEAT);
        ///treat items
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "flesh_treat"), FLESH_TREAT);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "fish_treat"), FISH_TREAT);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "berry_treat"), BERRY_TREAT);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "rooted_treat"), ROOTED_TREAT);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "grassy_treat"), GRASSY_TREAT);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "leafy_treat"), LEAFY_TREAT);
        ///stimulant items
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "flame_stimulant"), FLAME_STIMULANT);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "frost_stimulant"), FROST_STIMULANT);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "neutral_stimulant"), NEUTRAL_STIMULANT);
        ///thatch items
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "oak_thatch"), OAK_THATCH);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "birch_thatch"), BIRCH_THATCH);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "spruce_thatch"), SPRUCE_THATCH);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "jungle_thatch"), JUNGLE_THATCH);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "dark_oak_thatch"), DARK_OAK_THATCH);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "acacia_thatch"), ACACIA_THATCH);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "crimson_thatch"), CRIMSON_THATCH);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "warped_thatch"), WARPED_THATCH);
        //other resource items
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "grass_fiber"), GRASS_FIBER);
        ///tribute items
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "stegosaurus_plate"), STEGOSAURUS_PLATE);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "tyrannosaurus_arm"), TYRANNOSAURUS_ARM);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "dodo_beak"), DODO_BEAK);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "triceratops_horn"), TRICERATOPS_HORN);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "utahraptor_claw"), UTAHRAPTOR_CLAW);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "apatosaurus_vertebrae"), APATOSAURUS_VERTEBRAE);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "parasaurolophus_horn"), PARASAUROLOPHUS_HORN);
        Registry.register(Registry.ITEM, new Identifier(InitializeServer.MOD_ID, "dimetrodon_sail"), DIMETRODON_SAIL);
    }
}
