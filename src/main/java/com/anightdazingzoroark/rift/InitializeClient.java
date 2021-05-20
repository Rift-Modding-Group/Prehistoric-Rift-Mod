package com.anightdazingzoroark.rift;
import com.anightdazingzoroark.rift.registry.ModRenderers;
import net.fabricmc.api.ClientModInitializer;

public class InitializeClient implements ClientModInitializer{
    public static final String MOD_ID = "rift";

    @Override
    public void onInitializeClient() {
        ModRenderers.registerRenderers();
    }
}
