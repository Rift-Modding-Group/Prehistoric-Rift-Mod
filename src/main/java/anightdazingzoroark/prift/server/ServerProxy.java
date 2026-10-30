package anightdazingzoroark.prift.server;

import anightdazingzoroark.prift.RiftInitialize;

import anightdazingzoroark.prift.api.creature.RiftCreatureRegistrationEvent;
import anightdazingzoroark.prift.api.creature.RiftMoveRegistrationEvent;
import anightdazingzoroark.prift.api.creature.CreatureMoveRegistry;
import anightdazingzoroark.prift.server.entity.creatureMoves.RiftMoves;
import anightdazingzoroark.prift.server.block.RiftBlocks;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxGuiFactory;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxTileEntity;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.prift.server.config.RiftJsonConfigParser;
import anightdazingzoroark.prift.server.dataSerializers.InternalRegistryPrimer;
import anightdazingzoroark.prift.server.dataSerializers.PrimerEventHandler;
import anightdazingzoroark.prift.server.effect.RiftEffects;
import anightdazingzoroark.prift.server.entity.RiftEntities;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureGuiFactory;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureRegistry;
import anightdazingzoroark.prift.server.entity.model.CreatureModel;
import anightdazingzoroark.prift.server.item.RiftItems;
import anightdazingzoroark.prift.server.message.*;
import anightdazingzoroark.prift.server.properties.OtherEntityProperties;
import anightdazingzoroark.prift.server.properties.PlayerPartyProperties;
import anightdazingzoroark.prift.server.properties.PlayerCreatureBoxProperties;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.prift.server.world.RiftWorldGenerator;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import anightdazingzoroark.riftlib.message.RiftLibMessageSide;
import anightdazingzoroark.riftlib.nbtStorageUser.propertySystem.registry.PropertyRegistry;
import anightdazingzoroark.riftlib.resource.server.RiftLibCacheServer;
import com.cleanroommc.modularui.factory.GuiManager;
import net.minecraft.entity.EntityLivingBase;
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

        //register properties
        PropertyRegistry.register(
                PlayerPartyProperties.PROPERTY_NAME,
                new PropertyRegistry.ClassPropertyPair<>(EntityPlayer.class, PlayerPartyProperties::new)
        );
        PropertyRegistry.register(
                PlayerCreatureBoxProperties.PROPERTY_NAME,
                new PropertyRegistry.ClassPropertyPair<>(EntityPlayer.class, PlayerCreatureBoxProperties::new)
        );
        PropertyRegistry.register(
                OtherEntityProperties.PROPERTY_NAME,
                new PropertyRegistry.ClassPropertyPair<>(EntityLivingBase.class, OtherEntityProperties::new)
        );

        //register messages
        RiftMessages.WRAPPER.registerMessage(RiftPartyActionMessage.class, RiftLibMessageSide.SERVER);
        RiftMessages.WRAPPER.registerMessage(RiftRidingActionMessage.class, RiftLibMessageSide.SERVER);
        RiftMessages.WRAPPER.registerMessage(RiftRidingStartAimMessage.class, RiftLibMessageSide.SERVER);
        RiftMessages.WRAPPER.registerMessage(RiftRidingEndAimMessage.class, RiftLibMessageSide.SERVER);
        RiftMessages.WRAPPER.registerMessage(RiftRidingLeapMessage.class, RiftLibMessageSide.BOTH);

        //register events
        MinecraftForge.EVENT_BUS.register(new PrimerEventHandler(registryPrimer));
        MinecraftForge.EVENT_BUS.register(new ServerEvents());
        MinecraftForge.EVENT_BUS.register(new RiftSounds());

        //register effects
        RiftEffects.registerEffects();
        MinecraftForge.EVENT_BUS.register(new RiftEffects());

        // Moves must exist before any creature references them.
        RiftMoves.registerMoves();
        MinecraftForge.EVENT_BUS.post(new RiftMoveRegistrationEvent());
        CreatureMoveRegistry.finishRegistration();

        //register entities
        RiftCreatureRegistry.createCreatures();
        MinecraftForge.EVENT_BUS.post(new RiftCreatureRegistrationEvent((name, builder) -> RiftCreatureRegistry.registerCreatureType(builder)));
        RiftCreatureRegistry.finishCreatureRegistration();
        RiftEntities.registerEntities();

        //register GUIs
        GuiManager.registerFactory(RiftCreatureGuiFactory.INSTANCE);
        GuiManager.registerFactory(CreatureBoxGuiFactory.INSTANCE);

        //register configs
        jsonConfigParser = new RiftJsonConfigParser(e.getModConfigurationDirectory().toPath());

        //register blocks
        RiftBlocks.registerBlocks();
        GameRegistry.registerTileEntity(CreatureBoxTileEntity.class, new ResourceLocation(RiftInitialize.MODID, "creature_box"));
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
