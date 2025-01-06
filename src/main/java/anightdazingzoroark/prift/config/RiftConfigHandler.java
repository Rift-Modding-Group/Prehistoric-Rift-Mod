package anightdazingzoroark.prift.config;

import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RiftConfigHandler {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .addSerializationExclusionStrategy(new CustomExclusionStrategy())
            .create();
    private static final Map<String, RiftCreatureConfig> configs = new HashMap<>();
    private static File configDir;

    public static void init(File configDir, Map<String, Class<? extends RiftCreatureConfig>> configClasses) {
        RiftConfigHandler.configDir = configDir;
        if (!configDir.exists()) configDir.mkdirs();
        for (Map.Entry<String, Class<? extends RiftCreatureConfig>> entry : configClasses.entrySet()) {
            String configName = entry.getKey();
            Class<? extends RiftCreatureConfig> configClass = entry.getValue();
            File configFile = new File(configDir, configName + ".json");
            try {
                RiftCreatureConfig config = loadConfig(configFile, configClass);
                configs.put(configName, config);
                MinecraftForge.EVENT_BUS.register(config);
            }
            catch (IOException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static <T extends RiftCreatureConfig> T loadConfig(File configFile, Class<T> configClass) throws IOException, InstantiationException, IllegalAccessException {
        try (FileReader reader = new FileReader(configFile)) {
            return GSON.fromJson(reader, configClass);
        }
        catch (IOException e) {
            T config = configClass.newInstance();
            saveConfig(configFile, config);
            return config;
        }
    }

    private static void saveConfig(File configFile, RiftCreatureConfig config) throws IOException {
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(config, writer);
        }
    }

    public static RiftCreatureConfig getConfig(RiftCreatureType creatureType) {
        String configName = creatureType.name().toLowerCase();
        return configs.get(configName);
    }

    public static void saveAllConfigs() {
        for (Map.Entry<String, RiftCreatureConfig> entry : configs.entrySet()) {
            String configName = entry.getKey();
            RiftCreatureConfig config = entry.getValue();
            File configFile = new File(configDir, configName + ".json");
            try {
                saveConfig(configFile, config);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class CustomExclusionStrategy implements ExclusionStrategy {
        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            try {
                Object value = fieldAttributes.getDeclaringClass()
                        .getDeclaredField(fieldAttributes.getName())
                        .get(null); // Get the field's value
                return value == null; // Exclude if the field is null
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false; // Do not skip any class
        }
    }
}
