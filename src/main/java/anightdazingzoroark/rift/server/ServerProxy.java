package anightdazingzoroark.rift.server;

import anightdazingzoroark.rift.RiftConfig;
import anightdazingzoroark.rift.server.entity.RiftEntities;
import anightdazingzoroark.rift.server.items.RiftItems;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

@Mod.EventBusSubscriber
public class ServerProxy {

    public void preInit(FMLPreInitializationEvent e) {
        RiftItems.registerItems();
        MinecraftForge.EVENT_BUS.register(new RiftItems());
        RiftEntities.registerEntities();
    }

    public void init(FMLInitializationEvent event) {}

    public void postInit(FMLPostInitializationEvent event) {}

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {}

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {}
}
