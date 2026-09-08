package anightdazingzoroark.prift.server.sound;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.riftlib.sounds.RiftLibSoundEffect;
import anightdazingzoroark.riftlib.sounds.RiftLibSoundEffectRegistry;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RiftSounds {
    @NotNull
    private final List<ResourceLocation> livingSoundLocations = new ArrayList<>();

    public RiftSounds() {
        InputStream soundDefinitions = RiftSounds.class.getResourceAsStream("/assets/prift/sounds.json");
        if (soundDefinitions == null) {
            RiftInitialize.logger.error("Could not find Prehistoric Rift's sounds.json");
            return;
        }

        InputStreamReader reader = new InputStreamReader(soundDefinitions, StandardCharsets.UTF_8);
        try {
            JsonElement parsedDefinitions = JsonParser.parseReader(reader);
            reader.close();
            if (!parsedDefinitions.isJsonObject()) {
                RiftInitialize.logger.error("Prehistoric Rift's sounds.json does not contain a JSON object");
                return;
            }

            JsonObject soundEvents = parsedDefinitions.getAsJsonObject();
            for (String soundName : soundEvents.keySet()) {
                //idle, death, and hurt sounds
                if (soundName.endsWith(".idle") || soundName.endsWith(".hurt") || soundName.endsWith(".death")) {
                    this.livingSoundLocations.add(new ResourceLocation(RiftInitialize.MODID, soundName));
                }
                //other sounds for use in anims
                else {
                    RiftLibSoundEffectRegistry.registerSoundEffect(
                            RiftInitialize.MODID,
                            soundName,
                            new RiftLibSoundEffect(soundName).setPitch(0.95f, 1.05f)
                    );
                }
            }
        }
        catch (IOException | JsonParseException exception) {
            RiftInitialize.logger.error("Could not load Prehistoric Rift's sound events", exception);
        }
    }

    @SubscribeEvent
    public void onSoundRegistry(@NotNull RegistryEvent.Register<SoundEvent> event) {
        for (ResourceLocation soundLocation : this.livingSoundLocations) {
            event.getRegistry().register(new SoundEvent(soundLocation).setRegistryName(soundLocation));
        }
    }

    @Nullable
    public static SoundEvent getCreatureSound(@NotNull String creatureName, @NotNull String soundType) {
        return ForgeRegistries.SOUND_EVENTS.getValue(
                new ResourceLocation(RiftInitialize.MODID, RiftInitialize.MODID + "." + creatureName + "." + soundType)
        );
    }
}
