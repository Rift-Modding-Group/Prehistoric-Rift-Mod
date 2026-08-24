package anightdazingzoroark.prift.server;

import anightdazingzoroark.prift.api.creature.RiftCreatureRegistrationEvent;
import anightdazingzoroark.prift.server.config.RiftJsonConfigParser;
import anightdazingzoroark.prift.server.dataSerializers.InternalRegistryPrimer;
import anightdazingzoroark.prift.server.dataSerializers.PrimerEventHandler;
import anightdazingzoroark.prift.server.entity.RiftEntities;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureRegistry;
import anightdazingzoroark.prift.server.entity.model.CreatureModel;
import anightdazingzoroark.prift.server.item.RiftItems;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import anightdazingzoroark.riftlib.resource.server.RiftLibCacheServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod.EventBusSubscriber
public class ServerProxy {
    public static InternalRegistryPrimer registryPrimer;
    public static RiftJsonConfigParser jsonConfigParser;

    public void preInit(FMLPreInitializationEvent e) {
        registryPrimer = new InternalRegistryPrimer();

        //register events
        MinecraftForge.EVENT_BUS.register(new PrimerEventHandler(registryPrimer));
        MinecraftForge.EVENT_BUS.register(new ServerEvents());

        //register entities
        RiftCreatureRegistry.createCreatures();
        MinecraftForge.EVENT_BUS.post(new RiftCreatureRegistrationEvent(RiftCreatureRegistry::registerCreatureType));
        RiftCreatureRegistry.finishCreatureRegistration();
        RiftEntities.registerEntities();

        //register configs
        jsonConfigParser = new RiftJsonConfigParser(e.getModConfigurationDirectory().toPath());

        //register items
        RiftItems.registerItems();
        MinecraftForge.EVENT_BUS.register(new RiftItems());
        //RiftItems.registerOreDictionaryTags();
        RiftItems.registerFurnaceRecipes();

        //register server models
        ServerModelRegistry.registerServerModel(RiftCreature.class, CreatureModel::new);
    }

    public void init(FMLInitializationEvent event) {
        RiftLibCacheServer.getInstance().load();
    }

    public void postInit(FMLPostInitializationEvent event) {}
}
