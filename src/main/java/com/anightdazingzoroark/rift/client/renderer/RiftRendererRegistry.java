package com.anightdazingzoroark.rift.client.renderer;

import com.anightdazingzoroark.rift.RiftInitialize;
import com.anightdazingzoroark.rift.client.renderer.entity.TyrannosaurusRenderer;
import com.anightdazingzoroark.rift.client.renderer.entity.EggRenderer;
import com.anightdazingzoroark.rift.server.entities.RiftEntityRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RiftInitialize.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RiftRendererRegistry {
    @SubscribeEvent
    public static void registerRenderers(final FMLClientSetupEvent event) {
        EntityRenderers.register(RiftEntityRegistry.TYRANNOSAURUS.get(), TyrannosaurusRenderer::new);

        EntityRenderers.register(RiftEntityRegistry.TYRANNOSAURUS_EGG.get(), EggRenderer::new);
    }
}
