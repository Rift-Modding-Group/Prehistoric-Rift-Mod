package anightdazingzoroark.prift.server;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.crafttweaker.RiftCrafttweaker;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.compat.simpledifficulty.ModifierDimetrodon;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.blocks.RiftBlocks;
import anightdazingzoroark.prift.server.capabilities.CapabilityHandler;
import anightdazingzoroark.prift.server.capabilities.creatureBoxData.CreatureBoxData;
import anightdazingzoroark.prift.server.capabilities.creatureBoxData.CreatureBoxDataStorage;
import anightdazingzoroark.prift.server.capabilities.creatureBoxData.ICreatureBoxData;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.INonPotionEffects;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffects;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsStorage;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressStorage;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesStorage;
import anightdazingzoroark.prift.server.creatureSpawning.RiftCreatureSpawning;
import anightdazingzoroark.prift.server.dataSerializers.PrimerEventHandler;
import anightdazingzoroark.prift.server.effect.RiftEffects;
import anightdazingzoroark.prift.server.entity.RiftCreatureHitboxLinker;
import anightdazingzoroark.prift.server.entity.RiftEntities;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.events.RiftCreatureBoxBorder;
import anightdazingzoroark.prift.server.events.ServerEvents;
import anightdazingzoroark.prift.server.fluids.RiftFluids;
import anightdazingzoroark.prift.server.items.RiftItems;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.recipes.RiftRecipes;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntities;
import anightdazingzoroark.prift.server.world.RiftPlantGenerator;
import anightdazingzoroark.prift.server.world.RiftStructureGenerator;
import anightdazingzoroark.prift.server.dataSerializers.InternalRegistryPrimer;
import anightdazingzoroark.riftlib.RiftLibLinkerRegistry;
import com.charles445.simpledifficulty.api.temperature.TemperatureRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class ServerProxy {
    public static InternalRegistryPrimer registryPrimer;

    public void preInit(FMLPreInitializationEvent e) {
        registryPrimer = new InternalRegistryPrimer();
        MinecraftForge.EVENT_BUS.register(new PrimerEventHandler(registryPrimer));

        CapabilityManager.INSTANCE.register(IPlayerTamedCreatures.class, new PlayerTamedCreaturesStorage(), PlayerTamedCreatures::new);
        CapabilityManager.INSTANCE.register(IPlayerJournalProgress.class, new PlayerJournalProgressStorage(), PlayerJournalProgress::new);
        CapabilityManager.INSTANCE.register(INonPotionEffects.class, new NonPotionEffectsStorage(), NonPotionEffects::new);
        CapabilityManager.INSTANCE.register(ICreatureBoxData.class, new CreatureBoxDataStorage(), CreatureBoxData::new);
        MinecraftForge.EVENT_BUS.register(new CapabilityHandler());

        RiftMessages.registerMessages();
        NetworkRegistry.INSTANCE.registerGuiHandler(RiftInitialize.instance, new RiftGui());
        RiftFluids.registerFluids();
        RiftBlocks.registerBlocks();
        RiftItems.registerItems();
        RiftTileEntities.registerTileEntities();
        RiftItems.registerOreDicTags();
        RiftBlocks.registerOreDicTags();
        RiftRecipes.registerSmelting();
        RiftEffects.registerEffects();
        if (GeneralConfig.canUseMM()) RiftMMRecipes.registerRecipes();
        RiftCrafttweaker.loadCrafttweakerCompat();
        MinecraftForge.EVENT_BUS.register(new RiftItems());
        MinecraftForge.EVENT_BUS.register(new RiftBlocks());
        MinecraftForge.EVENT_BUS.register(new RiftEffects());
        RiftEntities.registerEntities();
        if (GeneralConfig.canUseSimpleDiff()) loadTemperatureRegistry();

        //load modded recipes
        if (GeneralConfig.canUseMM() && Loader.isModLoaded(RiftInitialize.HARVESTCRAFT_MOD_ID)) RiftMMRecipes.registerHarvestCraftRecipes();
    }

    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ServerEvents());
        MinecraftForge.EVENT_BUS.register(new RiftCreatureBoxBorder.RiftCreatureBorderHandler());
        MinecraftForge.EVENT_BUS.register(new RiftCreatureSpawning());
        GameRegistry.registerWorldGenerator(new RiftPlantGenerator(), 0);
        GameRegistry.registerWorldGenerator(new RiftStructureGenerator(), 0);
    }

    public void postInit(FMLPostInitializationEvent event) {}

    public void spawnParticle(String name, double x, double y, double z, double motX, double motY, double motZ) {}

    public void spawnParticle(String name, int color, double x, double y, double z, double motX, double motY, double motZ) {}

    public void spawnTrapParticle(int color, double x, double y, double z, double motX, double motY, double motZ) {}

    @Optional.Method(modid = RiftInitialize.SIMPLE_DIFFICULTY_MOD_ID)
    private void loadTemperatureRegistry() {
        TemperatureRegistry.registerModifier(new ModifierDimetrodon());
    }

    public int get3rdPersonView() {
        return 0;
    }

    public void set3rdPersonView(int view) {}

    public void setPreviousViewType(int view) {}

    public int getPreviousViewType() {
        return 0;
    }
}
