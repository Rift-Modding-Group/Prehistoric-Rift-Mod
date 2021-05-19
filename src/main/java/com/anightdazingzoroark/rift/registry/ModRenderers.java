package com.anightdazingzoroark.rift.registry;

import com.anightdazingzoroark.rift.client.renderers.TyrannosaurusRenderer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;

public class ModRenderers {
    public static void registerRenderers() {
        EntityRendererRegistry.INSTANCE.register(ModEntities.TYRANNOSAURUS, (dispatcher, context) -> {
            return new TyrannosaurusRenderer(dispatcher);
        });
    }
}
