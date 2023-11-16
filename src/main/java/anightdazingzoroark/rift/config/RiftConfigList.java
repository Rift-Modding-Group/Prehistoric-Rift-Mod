package anightdazingzoroark.rift.config;

import anightdazingzoroark.rift.RiftInitialize;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.lang.reflect.Constructor;

public enum RiftConfigList {
    GENERAL(GeneralConfig.class),
    TYRANNOSAURUS(TyrannosaurusConfig.class),
    STEGOSAURUS(StegosaurusConfig.class),
    DODO(DodoConfig.class);

    private final Class<? extends RiftConfig> configClass;
    private RiftConfig configInstance;

    RiftConfigList(Class configClass) {
        this.configClass = configClass;
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
