package com.anightdazingzoroark.rift;

import com.anightdazingzoroark.rift.registry.ModItems;
import net.fabricmc.api.ModInitializer;

public class Items implements ModInitializer {
    public static final String MOD_ID = "rift";

    @Override
    public void onInitialize() {
        System.out.println("Items!");
        ModItems.registerItems();
    }
}
