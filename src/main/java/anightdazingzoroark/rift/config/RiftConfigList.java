package anightdazingzoroark.rift.config;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.server.entity.creature.Dodo;
import anightdazingzoroark.rift.server.entity.creature.RiftCreature;
import anightdazingzoroark.rift.server.entity.creature.Stegosaurus;
import anightdazingzoroark.rift.server.entity.creature.Tyrannosaurus;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.lang.reflect.Constructor;

public enum RiftConfigList {
    GENERAL(null, GeneralConfig.class),
    TYRANNOSAURUS(Tyrannosaurus.class, TyrannosaurusConfig.class),
    STEGOSAURUS(Stegosaurus.class, StegosaurusConfig.class),
    DODO(Dodo.class, DodoConfig.class);

    private final Class<? extends RiftCreature> creatureClass;
    private final Class<? extends RiftConfig> configClass;
    private RiftConfig configInstance;

    RiftConfigList(Class<? extends RiftCreature> creatureClass, Class<? extends RiftConfig> configClass) {
        this.creatureClass = creatureClass;
        this.configClass = configClass;
    }

    public Class getCreatureClass() {
        return this.creatureClass;
    }

    public void loadConfig(File directory) {
        try {
            Constructor<? extends RiftConfig> constructor = this.configClass.getConstructor(Configuration.class);
            String configName = this.name().toLowerCase() + ".cfg";
            File configFile = new File(directory, "rift/" + configName);
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
