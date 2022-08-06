package com.anightdazingzoroark.rift;

import com.anightdazingzoroark.rift.client.renderer.entity.TyrannosaurusRenderer;
import com.anightdazingzoroark.rift.server.entities.RiftEntityRegistry;
import com.anightdazingzoroark.rift.server.items.RiftItemRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib3.GeckoLib;

@Mod(RiftInitialize.MODID)
public class RiftInitialize {
    public static final String MODID = "rift";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final CreativeModeTab RIFT_ITEMS_TAB = new CreativeModeTab("rift.items_tab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(RiftItemRegistry.TYRANNOSAURUS_ARM.get());
        }
    };

    public RiftInitialize() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        RiftItemRegistry.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
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
