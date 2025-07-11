package anightdazingzoroark.prift;

import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.config.*;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.commands.RiftBleedCommand;
import anightdazingzoroark.prift.server.commands.RiftCreatureHighlightCommand;
import anightdazingzoroark.prift.server.commands.RiftJournalCommand;
import anightdazingzoroark.prift.server.commands.RiftResetWildCreaturesCommand;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.riftlib.RiftLib;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Mod(modid = RiftInitialize.MODID, name = RiftInitialize.MODNAME, version = RiftInitialize.MODVERSION, dependencies = "required-after:forge@[11.16.0.1865,);required-after:riftlib@[1.0.0,);required-after:mixinbooter@[10.2,);after:harvestcraft@[1.12.2zb,);after:pyrotech@[1.12.2-1.6.11,);")
public class RiftInitialize {
    public static final String MODID = "prift";
    public static final String SSR_MOD_ID = "shouldersurfing";
    public static final String PYROTECH_MOD_ID = "pyrotech";
    public static final String SIMPLE_DIFFICULTY_MOD_ID = "simpledifficulty";
    public static final String MYSTICAL_MECHANICS_MOD_ID = "mysticalmechanics";
    public static final String HARVESTCRAFT_MOD_ID = "harvestcraft";
    public static final String SERENE_SEASONS_MOD_ID = "sereneseasons";
    public static final String GAME_STAGES_MOD_ID = "gamestages";
    public static final String JEI_MOD_ID = "jei";
    public static final String MODNAME = "Prehistoric Rift";
    public static final String MODVERSION= "0.0.16";
    @SidedProxy(clientSide = "anightdazingzoroark.prift.client.ClientProxy", serverSide = "anightdazingzoroark.prift.server.ServerProxy")
    public static ServerProxy PROXY;
    @Mod.Instance(MODID)
    public static RiftInitialize instance;
    public static Logger logger;
    public static Configuration configMain;

    static {
        FluidRegistry.enableUniversalBucket();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        PROXY.preInit(event);

        //for general config
        File directory = event.getModConfigurationDirectory();
        configMain = new Configuration(new File(directory.getPath(), "prift/general.cfg"));
        GeneralConfig.readConfig();

        //for creature config
        Map<String, Class<? extends RiftCreatureConfig>> configClasses = new HashMap<>();
        for (RiftCreatureType creatureType : RiftCreatureType.values()) {
            if (creatureType.getConfig() != null) configClasses.put(creatureType.name().toLowerCase(), creatureType.getConfig());
        }
        RiftConfigHandler.init(new File(event.getModConfigurationDirectory(), "prift/creatures/"), configClasses);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        RiftLib.initialize();
        PROXY.init(event);

        logger.info("MOMMY AYUNDA PLEASE BREASTFEED MEEEEEEEEEEEEEEE");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        PROXY.postInit(event);

        //for general config
        if (configMain.hasChanged()) configMain.save();

        //for creature config
        RiftConfigHandler.saveAllConfigs();
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new RiftBleedCommand());
        event.registerServerCommand(new RiftCreatureHighlightCommand());
        event.registerServerCommand(new RiftJournalCommand());
        event.registerServerCommand(new RiftResetWildCreaturesCommand());
    }
}
