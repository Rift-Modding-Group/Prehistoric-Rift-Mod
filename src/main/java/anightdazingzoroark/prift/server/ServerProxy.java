package anightdazingzoroark.prift.server;

import anightdazingzoroark.prift.api.creature.RiftCreatureRegistrationEvent;
import anightdazingzoroark.prift.server.block.RiftBlocks;
import anightdazingzoroark.prift.server.config.RiftJsonConfigParser;
import anightdazingzoroark.prift.server.dataSerializers.InternalRegistryPrimer;
import anightdazingzoroark.prift.server.dataSerializers.PrimerEventHandler;
import anightdazingzoroark.prift.server.entity.RiftEntities;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureGuiFactory;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureRegistry;
import anightdazingzoroark.prift.server.entity.model.CreatureModel;
import anightdazingzoroark.prift.server.item.RiftItems;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftPartyActionMessage;
import anightdazingzoroark.prift.server.player.PlayerPartyProperties;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.prift.server.world.RiftWorldGenerator;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import anightdazingzoroark.riftlib.message.RiftLibMessageSide;
import anightdazingzoroark.riftlib.nbtStorageUser.propertySystem.registry.PropertyRegistry;
import anightdazingzoroark.riftlib.resource.server.RiftLibCacheServer;
import com.cleanroommc.modularui.factory.GuiManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;

@Mod.EventBusSubscriber
public class ServerProxy {
    public static InternalRegistryPrimer registryPrimer;
    public static RiftJsonConfigParser jsonConfigParser;

    public void preInit(FMLPreInitializationEvent e) {
        registryPrimer = new InternalRegistryPrimer();

        PropertyRegistry.register(
                PlayerPartyProperties.PROPERTY_NAME,
                new PropertyRegistry.ClassPropertyPair<>(EntityPlayer.class, PlayerPartyProperties::new)
        );
        RiftMessages.WRAPPER.registerMessage(RiftPartyActionMessage.class, RiftLibMessageSide.SERVER);

        //register events
        MinecraftForge.EVENT_BUS.register(new PrimerEventHandler(registryPrimer));
        MinecraftForge.EVENT_BUS.register(new ServerEvents());
        MinecraftForge.EVENT_BUS.register(new RiftSounds());

        //register entities
        RiftCreatureRegistry.createCreatures();
        MinecraftForge.EVENT_BUS.post(new RiftCreatureRegistrationEvent(RiftCreatureRegistry::registerCreatureType));
        RiftCreatureRegistry.finishCreatureRegistration();
        RiftEntities.registerEntities();

        //register GUIs
        GuiManager.registerFactory(RiftCreatureGuiFactory.INSTANCE);

        //register configs
        jsonConfigParser = new RiftJsonConfigParser(e.getModConfigurationDirectory().toPath());

        //register blocks
        RiftBlocks.registerBlocks();
        MinecraftForge.EVENT_BUS.register(new RiftBlocks());

        //register items
        RiftItems.registerItems();
        MinecraftForge.EVENT_BUS.register(new RiftItems());
        //RiftItems.registerOreDictionaryTags();
        RiftItems.registerFurnaceRecipes();

        //register world generation
        GameRegistry.registerWorldGenerator(new RiftWorldGenerator(), 0);

        //register server models
        ServerModelRegistry.registerServerModel(RiftCreature.class, CreatureModel::new);
    }

    public void init(FMLInitializationEvent event) {
        RiftLibCacheServer.getInstance().load();
    }

    public void postInit(FMLPostInitializationEvent event) {}
}
