package anightdazingzoroark.rift;

import anightdazingzoroark.rift.server.ServerProxy;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib3.GeckoLib;

import java.io.File;

@Mod(modid = RiftInitialize.MODID, name = RiftInitialize.MODNAME, version = RiftInitialize.MODVERSION, dependencies = "required-after:forge@[11.16.0.1865,)")
public class RiftInitialize {
    public static final String MODID = "rift";
    public static final String MODNAME = "Prehistoric Rift";
    public static final String MODVERSION= "0.0.1";
    @SidedProxy(clientSide = "anightdazingzoroark.rift.client.ClientProxy", serverSide = "anightdazingzoroark.rift.server.ServerProxy")
    public static ServerProxy PROXY;
    @Mod.Instance
    public static RiftInitialize instance;
    public static Logger logger;
    public static Configuration config;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        PROXY.preInit(event);

        //for config
        File directory = event.getModConfigurationDirectory();
        config = new Configuration(new File(directory.getPath(), "rift.cfg"));
        RiftConfig.readConfig();
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
        if (config.hasChanged()) {
            config.save();
        }
    }
}
