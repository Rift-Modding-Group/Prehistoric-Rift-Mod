package com.anightdazingzoroark.rift;

import net.fabricmc.api.ModInitializer;
import software.bernie.geckolib3.GeckoLib;

public class Models implements ModInitializer {
    public static final String MOD_ID = "rift";

    @Override
    public void onInitialize() {
        System.out.println("Models!");
        GeckoLib.initialize();
    }
}
