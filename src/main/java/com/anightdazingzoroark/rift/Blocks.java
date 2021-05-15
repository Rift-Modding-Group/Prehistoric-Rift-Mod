package com.anightdazingzoroark.rift;

import com.anightdazingzoroark.rift.registry.ModBlocks;
import net.fabricmc.api.ModInitializer;

public class Blocks implements ModInitializer {
    public static final String MOD_ID = "rift";

    @Override
    public void onInitialize() {
        System.out.println("Blocks!");
        ModBlocks.registerBlocks();
    }
}
