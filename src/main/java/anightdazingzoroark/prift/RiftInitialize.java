package anightdazingzoroark.prift;

import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.riftlib.RiftLib;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = RiftInitialize.MODID,
        name = RiftInitialize.MODNAME,
        version = RiftInitialize.MODVERSION,
        dependencies = "required-after:forge@[11.16.0.1865,);"
                +"required-after:riftlib@[1.0.0,);"
                +"required-after:modularui@[3.1.0,);"
                +"after:harvestcraft@[1.12.2zb,);"
                +"after:pyrotech@[1.12.2-1.6.11,);"
)
public class RiftInitialize {
    public static final String MODID = "prift";
    public static final String SSR_MOD_ID = "shouldersurfing";
    public static final String PYROTECH_MOD_ID = "pyrotech";
    public static final String SIMPLE_DIFFICULTY_MOD_ID = "simpledifficulty";
    public static final String MYSTICAL_MECHANICS_MOD_ID = "mysticalmechanics";
    public static final String HARVESTCRAFT_MOD_ID = "harvestcraft";
    public static final String SERENE_SEASONS_MOD_ID = "sereneseasons";
    public static final String GAME_STAGES_MOD_ID = "gamestages";
    public static final String MODULAR_UI_ID = "modularui";
    public static final String JEI_MOD_ID = "jei";
    public static final String MODNAME = "Prehistoric Rift";
    public static final String MODVERSION= "0.1.0";
    @SidedProxy(clientSide = "anightdazingzoroark.prift.client.ClientProxy", serverSide = "anightdazingzoroark.prift.server.ServerProxy")
    public static ServerProxy PROXY;
    @Mod.Instance(MODID)
    public static RiftInitialize instance;
    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        //init logger
        logger = event.getModLog();

        //init mod content
        PROXY.preInit(event);

        //init mob families
        //RiftMobFamilies.initMobFamilies(directory);
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
    }

    /*
    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new RiftBleedCommand());
        event.registerServerCommand(new RiftCreatureHighlightCommand());
        event.registerServerCommand(new RiftJournalCommand());
        event.registerServerCommand(new RiftResetWildCreaturesCommand());
    }
     */
}
