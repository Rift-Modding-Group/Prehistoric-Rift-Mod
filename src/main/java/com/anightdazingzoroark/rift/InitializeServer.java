package com.anightdazingzoroark.rift;

import com.anightdazingzoroark.rift.registry.ModBlocks;
import com.anightdazingzoroark.rift.registry.ModEntities;
import com.anightdazingzoroark.rift.registry.ModItems;
import com.anightdazingzoroark.rift.registry.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.GeckoLib;

public class InitializeServer implements ModInitializer {
    public static final String MOD_ID = "rift";

    public static final ItemGroup RIFT_ITEMS = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "rift_items"),
            () -> new ItemStack(ModItems.TYRANNOSAURUS_ARM)
    );

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        System.out.println("Prehistoric Rift Mod!");
        ModBlocks.registerBlocks();
        ModItems.registerItems();
        ModEntities.registerEntities();
        ModSounds.registerSounds();
        GeckoLib.initialize();
    }
}
