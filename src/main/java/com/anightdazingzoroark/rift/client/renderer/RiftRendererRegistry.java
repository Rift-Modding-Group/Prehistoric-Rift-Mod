package com.anightdazingzoroark.rift.client.renderer;

import com.anightdazingzoroark.rift.client.renderer.entity.TyrannosaurusRenderer;
import com.anightdazingzoroark.rift.server.entities.RiftEntityRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class RiftRendererRegistry {
    @SubscribeEvent
    public static void registerRenderers(final FMLClientSetupEvent event) {
        EntityRenderers.register(RiftEntityRegistry.TYRANNOSAURUS.get(), TyrannosaurusRenderer::new);
    }
}
