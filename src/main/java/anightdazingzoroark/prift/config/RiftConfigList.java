package anightdazingzoroark.prift.config;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.*;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.lang.reflect.Constructor;

public enum RiftConfigList {
    GENERAL(null, GeneralConfig.class, null),
    TYRANNOSAURUS(Tyrannosaurus.class, TyrannosaurusConfig.class, RiftSpawnType.LAND),
    STEGOSAURUS(Stegosaurus.class, StegosaurusConfig.class, RiftSpawnType.LAND),
    DODO(Dodo.class, DodoConfig.class, RiftSpawnType.LAND),
    TRICERATOPS(Triceratops.class, TriceratopsConfig.class, RiftSpawnType.LAND),
    UTAHRAPTOR(Utahraptor.class, UtahraptorConfig.class, RiftSpawnType.LAND),
    APATOSAURUS(Apatosaurus.class, ApatosaurusConfig.class, RiftSpawnType.LAND),
    PARASAUROLOPHUS(Parasaurolophus.class, ParasaurolophusConfig.class, RiftSpawnType.LAND),
    DIMETRODON(Dimetrodon.class, DimetrodonConfig.class, RiftSpawnType.LAND),
    COELACANTH(Coelacanth.class, CoelacanthConfig.class, RiftSpawnType.WATER),
    MEGAPIRANHA(Megapiranha.class, MegapiranhaConfig.class, RiftSpawnType.WATER),
    SARCOSUCHUS(Sarcosuchus.class, SarcosuchusConfig.class, RiftSpawnType.WATER),
    ANOMALOCARIS(Anomalocaris.class, AnomalocarisConfig.class, RiftSpawnType.WATER),
    SAUROPHAGANAX(Saurophaganax.class, SaurophaganaxConfig.class, RiftSpawnType.LAND),
    DIREWOLF(Direwolf.class, DirewolfConfig.class, RiftSpawnType.LAND),
    MEGALOCEROS(Megaloceros.class, MegalocerosConfig.class, RiftSpawnType.LAND),
    BARYONYX(Baryonyx.class, BaryonyxConfig.class, RiftSpawnType.WATER),
    PALAEOCASTOR(Palaeocastor.class, PalaeocastorConfig.class, RiftSpawnType.UNDERGROUND);

    private final Class<? extends RiftCreature> creatureClass;
    private final Class<? extends RiftConfig> configClass;
    private RiftConfig configInstance;
    private RiftSpawnType spawnType;

    RiftConfigList(Class<? extends RiftCreature> creatureClass, Class<? extends RiftConfig> configClass, RiftSpawnType spawnType) {
        this.creatureClass = creatureClass;
        this.configClass = configClass;
    }

    public Class<? extends RiftCreature> getCreatureClass() {
        return this.creatureClass;
    }

    public RiftSpawnType getSpawnType() {
        return this.spawnType;
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

    public enum RiftSpawnType {
        LAND,
        WATER,
        UNDERGROUND;
    }
}
