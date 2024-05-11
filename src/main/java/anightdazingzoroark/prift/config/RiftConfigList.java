package anightdazingzoroark.prift.config;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.*;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.lang.reflect.Constructor;

public enum RiftConfigList {
    GENERAL(null, GeneralConfig.class, false),
    TYRANNOSAURUS(Tyrannosaurus.class, TyrannosaurusConfig.class, false),
    STEGOSAURUS(Stegosaurus.class, StegosaurusConfig.class, false),
    DODO(Dodo.class, DodoConfig.class, false),
    TRICERATOPS(Triceratops.class, TriceratopsConfig.class, false),
    UTAHRAPTOR(Utahraptor.class, UtahraptorConfig.class, false),
    APATOSAURUS(Apatosaurus.class, ApatosaurusConfig.class, false),
    PARASAUROLOPHUS(Parasaurolophus.class, ParasaurolophusConfig.class, false),
    DIMETRODON(Dimetrodon.class, DimetrodonConfig.class, false),
    COELACANTH(Coelacanth.class, CoelacanthConfig.class, true),
    MEGAPIRANHA(Megapiranha.class, MegapiranhaConfig.class, true),
    SARCOSUCHUS(Sarcosuchus.class, SarcosuchusConfig.class, true),
    ANOMALOCARIS(Anomalocaris.class, AnomalocarisConfig.class, true),
    SAUROPHAGANAX(Saurophaganax.class, SaurophaganaxConfig.class, false),
    DIREWOLF(Direwolf.class, DirewolfConfig.class, false);

    private final Class<? extends RiftCreature> creatureClass;
    private final Class<? extends RiftWaterCreature> waterCreatureClass;
    private final Class<? extends RiftConfig> configClass;
    private RiftConfig configInstance;
    private boolean isWaterCreature;

    RiftConfigList(Class<? extends RiftCreature> creatureClass, Class<? extends RiftConfig> configClass, boolean isWaterCreature) {
        this.creatureClass = creatureClass;
        this.configClass = configClass;
        this.isWaterCreature = isWaterCreature;
        if (isWaterCreature) this.waterCreatureClass = (Class<? extends RiftWaterCreature>)this.creatureClass;
        else this.waterCreatureClass = null;
    }

    public Class<? extends RiftCreature> getCreatureClass() {
        return this.creatureClass;
    }

    public Class<? extends RiftWaterCreature> getWaterCreatureClass() {
        return this.waterCreatureClass;
    }

    public boolean getIsWaterCreature() {
        return this.isWaterCreature;
    }

    public void loadConfig(File directory) {
        try {
            Constructor<? extends RiftConfig> constructor = this.configClass.getConstructor(Configuration.class);
            String configName = this.name().toLowerCase() + ".cfg";
            File configFile = new File(directory, "prift/" + configName);
            Configuration cfg = new Configuration(configFile);

            this.configInstance = constructor.newInstance(cfg);
            configInstance.init();

            if (cfg.hasChanged()) {
                cfg.save();
            }
        }
        catch (Exception e) {
            String configName = this.name().toLowerCase();
            RiftInitialize.logger.log(Level.ERROR, "Problem loading config file "+configName+".cfg!", e);
        }
    }

    public RiftConfig getConfigInstance() {
        return this.configInstance;
    }
}
