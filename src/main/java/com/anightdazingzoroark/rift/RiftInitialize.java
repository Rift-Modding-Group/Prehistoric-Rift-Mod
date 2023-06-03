package com.anightdazingzoroark.rift;

import com.anightdazingzoroark.rift.server.client.renderer.RiftRendererRegistry;
import com.anightdazingzoroark.rift.server.client.sounds.RiftSoundRegistry;
import com.anightdazingzoroark.rift.server.entities.RiftEntityRegistry;
import com.anightdazingzoroark.rift.server.items.RiftCreativeTab;
import com.anightdazingzoroark.rift.server.items.RiftItemRegistry;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(RiftInitialize.MODID)
public class RiftInitialize {
    public static final String MODID = "rift";
    private static final Logger LOGGER = LogUtils.getLogger();

    public RiftInitialize() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        RiftItemRegistry.register(modEventBus);
        RiftEntityRegistry.register(modEventBus);
        modEventBus.addListener(RiftCreativeTab::addCreative);

        RiftSoundRegistry.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(RiftRendererRegistry::registerRenderers);

        MinecraftForge.EVENT_BUS.register(this);
        GeckoLib.initialize();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {}

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {}
    }
}
