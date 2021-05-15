package com.anightdazingzoroark.rift;

import com.anightdazingzoroark.rift.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class CreativeTab implements ModInitializer {
    public static final String MOD_ID = "rift";

    public static final ItemGroup RIFT_ITEMS = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "rift_items"),
            () -> new ItemStack(ModItems.TYRANNOSAURUS_ARM)
    );

    @Override
    public void onInitialize() {
        System.out.println("Creative Tab!");
    }
}
