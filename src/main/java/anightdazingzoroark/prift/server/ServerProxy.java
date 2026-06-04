package anightdazingzoroark.prift.server;

import anightdazingzoroark.prift.server.dataSerializers.InternalRegistryPrimer;
import anightdazingzoroark.prift.server.dataSerializers.PrimerEventHandler;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod.EventBusSubscriber
public class ServerProxy {
    public static InternalRegistryPrimer registryPrimer;

    public void preInit(FMLPreInitializationEvent e) {
        registryPrimer = new InternalRegistryPrimer();
        MinecraftForge.EVENT_BUS.register(new PrimerEventHandler(registryPrimer));

        RiftCreatureRegistry.createCreatures();
    }

    public void init(FMLInitializationEvent event) {}

    public void postInit(FMLPostInitializationEvent event) {}
}
