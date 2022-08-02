package com.anightdazingzoroark.rift.registry;

import com.anightdazingzoroark.rift.InitializeServer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModBlocks {
    public static final Block OAK_THATCH_BLOCK = new Block(FabricBlockSettings
        .of(Material.WOOD)
        .breakByHand(true)
        .breakByTool(FabricToolTags.AXES, 0)
        .strength(0.75f, 2f)
        .sounds(BlockSoundGroup.WOOD));
    public static final Block BIRCH_THATCH_BLOCK = new Block(FabricBlockSettings
            .of(Material.WOOD)
            .breakByHand(true)
            .breakByTool(FabricToolTags.AXES, 0)
            .strength(0.75f, 2f)
            .sounds(BlockSoundGroup.WOOD));
    public static final Block SPRUCE_THATCH_BLOCK = new Block(FabricBlockSettings
            .of(Material.WOOD)
            .breakByHand(true)
            .breakByTool(FabricToolTags.AXES, 0)
            .strength(0.75f, 2f)
            .sounds(BlockSoundGroup.WOOD));
    public static final Block JUNGLE_THATCH_BLOCK = new Block(FabricBlockSettings
            .of(Material.WOOD)
            .breakByHand(true)
            .breakByTool(FabricToolTags.AXES, 0)
            .strength(0.75f, 2f)
            .sounds(BlockSoundGroup.WOOD));
    public static final Block DARK_OAK_THATCH_BLOCK = new Block(FabricBlockSettings
            .of(Material.WOOD)
            .breakByHand(true)
            .breakByTool(FabricToolTags.AXES, 0)
            .strength(0.75f, 2f)
            .sounds(BlockSoundGroup.WOOD));
    public static final Block ACACIA_THATCH_BLOCK = new Block(FabricBlockSettings
            .of(Material.WOOD)
            .breakByHand(true)
            .breakByTool(FabricToolTags.AXES, 0)
            .strength(0.75f, 2f)
            .sounds(BlockSoundGroup.WOOD));
    public static final Block CRIMSON_THATCH_BLOCK = new Block(FabricBlockSettings
            .of(Material.WOOD)
            .breakByHand(true)
            .breakByTool(FabricToolTags.AXES, 0)
            .strength(0.75f, 2f)
            .sounds(BlockSoundGroup.WOOD));
    public static final Block WARPED_THATCH_BLOCK = new Block(FabricBlockSettings
            .of(Material.WOOD)
            .breakByHand(true)
            .breakByTool(FabricToolTags.AXES, 0)
            .strength(0.75f, 2f)
            .sounds(BlockSoundGroup.WOOD));

    public static void registerBlocks() {
        //thatch blocks
        Registry.register(Registry.BLOCK, new Identifier(InitializeServer.MOD_ID, "oak_thatch_block"), OAK_THATCH_BLOCK);
        Registry.register(Registry.BLOCK, new Identifier(InitializeServer.MOD_ID, "birch_thatch_block"), BIRCH_THATCH_BLOCK);
        Registry.register(Registry.BLOCK, new Identifier(InitializeServer.MOD_ID, "spruce_thatch_block"), SPRUCE_THATCH_BLOCK);
        Registry.register(Registry.BLOCK, new Identifier(InitializeServer.MOD_ID, "jungle_thatch_block"), JUNGLE_THATCH_BLOCK);
        Registry.register(Registry.BLOCK, new Identifier(InitializeServer.MOD_ID, "dark_oak_thatch_block"), DARK_OAK_THATCH_BLOCK);
        Registry.register(Registry.BLOCK, new Identifier(InitializeServer.MOD_ID, "acacia_thatch_block"), ACACIA_THATCH_BLOCK);
        Registry.register(Registry.BLOCK, new Identifier(InitializeServer.MOD_ID, "crimson_thatch_block"), CRIMSON_THATCH_BLOCK);
        Registry.register(Registry.BLOCK, new Identifier(InitializeServer.MOD_ID, "warped_thatch_block"), WARPED_THATCH_BLOCK);

        //flammability for thatch blocks
        FlammableBlockRegistry flammableRegistry = FlammableBlockRegistry.getDefaultInstance();
        flammableRegistry.add(OAK_THATCH_BLOCK, 10, 10);
        flammableRegistry.add(BIRCH_THATCH_BLOCK, 10, 10);
        flammableRegistry.add(SPRUCE_THATCH_BLOCK, 10, 10);
        flammableRegistry.add(JUNGLE_THATCH_BLOCK, 10, 10);
        flammableRegistry.add(DARK_OAK_THATCH_BLOCK, 10, 10);
        flammableRegistry.add(ACACIA_THATCH_BLOCK, 10, 10);
    }
}
