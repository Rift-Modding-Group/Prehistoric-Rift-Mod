package anightdazingzoroark.rift.client;

import anightdazingzoroark.rift.RiftInitialize;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(RiftInitialize.MODID)
public class RiftSounds {
    @GameRegistry.ObjectHolder("rift.tyrannosaurus.idle")
    public static final SoundEvent TYRANNOSAURUS_IDLE = createSoundEvent("rift.tyrannosaurus.idle");

    @GameRegistry.ObjectHolder("rift.tyrannosaurus.hurt")
    public static final SoundEvent TYRANNOSAURUS_HURT = createSoundEvent("rift.tyrannosaurus.hurt");

    @GameRegistry.ObjectHolder("rift.tyrannosaurus.death")
    public static final SoundEvent TYRANNOSAURUS_DEATH = createSoundEvent("rift.tyrannosaurus.death");

    @GameRegistry.ObjectHolder("rift.tyrannosaurus.roar")
    public static final SoundEvent TYRANNOSAURUS_ROAR = createSoundEvent("rift.tyrannosaurus.roar");

    @GameRegistry.ObjectHolder("rift.stegosaurus.idle")
    public static final SoundEvent STEGOSAURUS_IDLE = createSoundEvent("rift.stegosaurus.idle");

    @GameRegistry.ObjectHolder("rift.stegosaurus.hurt")
    public static final SoundEvent STEGOSAURUS_HURT = createSoundEvent("rift.stegosaurus.hurt");

    @GameRegistry.ObjectHolder("rift.stegosaurus.death")
    public static final SoundEvent STEGOSAURUS_DEATH = createSoundEvent("rift.stegosaurus.death");

    private static SoundEvent createSoundEvent(String soundName) {
        ResourceLocation soundID = new ResourceLocation(RiftInitialize.MODID, soundName);
        return new SoundEvent(soundID).setRegistryName(soundID);
    }
}
