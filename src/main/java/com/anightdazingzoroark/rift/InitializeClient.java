package com.anightdazingzoroark.rift;
import com.anightdazingzoroark.rift.registry.ModRenderers;
import net.fabricmc.api.ClientModInitializer;

public class InitializeClient implements ClientModInitializer{
    @Override
    public void onInitializeClient() {
        ModRenderers.registerRenderers();
    }
}
