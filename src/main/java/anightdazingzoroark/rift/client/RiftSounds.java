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

    @GameRegistry.ObjectHolder("rift.dodo.idle")
    public static final SoundEvent DODO_IDLE = createSoundEvent("rift.dodo.idle");

    @GameRegistry.ObjectHolder("rift.dodo.hurt")
    public static final SoundEvent DODO_HURT = createSoundEvent("rift.dodo.hurt");

    @GameRegistry.ObjectHolder("rift.dodo.death")
    public static final SoundEvent DODO_DEATH = createSoundEvent("rift.dodo.death");

    @GameRegistry.ObjectHolder("rift.triceratops.idle")
    public static final SoundEvent TRICERATOPS_IDLE = createSoundEvent("rift.triceratops.idle");

    @GameRegistry.ObjectHolder("rift.triceratops.hurt")
    public static final SoundEvent TRICERATOPS_HURT = createSoundEvent("rift.triceratops.hurt");

    @GameRegistry.ObjectHolder("rift.triceratops.death")
    public static final SoundEvent TRICERATOPS_DEATH = createSoundEvent("rift.triceratops.death");

    private static SoundEvent createSoundEvent(String soundName) {
        ResourceLocation soundID = new ResourceLocation(RiftInitialize.MODID, soundName);
        return new SoundEvent(soundID).setRegistryName(soundID);
    }
}
