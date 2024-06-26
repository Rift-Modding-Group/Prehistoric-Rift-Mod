package anightdazingzoroark.prift;

import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.config.RiftConfigList;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.commands.RiftBleedCommand;
import anightdazingzoroark.prift.server.commands.RiftJournalCommand;
import anightdazingzoroark.prift.server.entity.RiftEntities;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib3.GeckoLib;

import java.io.File;

@Mod(modid = RiftInitialize.MODID, name = RiftInitialize.MODNAME, version = RiftInitialize.MODVERSION, dependencies = "required-after:forge@[11.16.0.1865,);required-after:llibrary@[1.7.19,);required-after:geckolib3@[3.0.19,)")
public class RiftInitialize {
    public static final String MODID = "prift";
    public static final String SSR_MOD_ID = "shouldersurfing";
    public static final String PYROTECH_MOD_ID = "pyrotech";
    public static final String SIMPLE_DIFFICULTY_MOD_ID = "simpledifficulty";
    public static final String MYSTICAL_MECHANICS_MOD_ID = "mysticalmechanics";
    public static final String MODNAME = "Prehistoric Rift";
    public static final String MODVERSION= "0.0.11";
    @SidedProxy(clientSide = "anightdazingzoroark.prift.client.ClientProxy", serverSide = "anightdazingzoroark.prift.server.ServerProxy")
    public static ServerProxy PROXY;
    @Mod.Instance(MODID)
    public static RiftInitialize instance;
    public static Logger logger;

    static {
        FluidRegistry.enableUniversalBucket();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        PROXY.preInit(event);

        //for config
        File directory = event.getModConfigurationDirectory();
        for (RiftConfigList configVal : RiftConfigList.values()) {
            configVal.loadConfig(directory);
        }

        RiftEntities.registerSpawn();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        PROXY.init(event);
        GeckoLib.initialize();

        logger.info("MOMMY AYUNDA PLEASE BREASTFEED MEEEEEEEEEEEEEEE");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        PROXY.postInit(event);

        //for config
        for (RiftConfigList configVal : RiftConfigList.values()) {
            Configuration cfg = configVal.getConfigInstance().config;
            if (cfg.hasChanged()) {
                cfg.save();
            }
        }

        //crash game if some config stuff is wrong
        if (!GeneralConfig.minRevivalDiff.equals("PEACEFUL") && !GeneralConfig.minRevivalDiff.equals("EASY") && !GeneralConfig.minRevivalDiff.equals("NORMAL") && !GeneralConfig.minRevivalDiff.equals("HARD") && !GeneralConfig.minRevivalDiff.equals("NONE")) {
            throw new RuntimeException("Invalid configuration value detected for field \"Minimum Difficulty for Creature Revival\" in general.cfg. Please correct your configuration file.");
        }
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new RiftBleedCommand());
        event.registerServerCommand(new RiftJournalCommand());
    }
}
