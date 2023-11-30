package anightdazingzoroark.prift.client;

import anightdazingzoroark.prift.RiftInitialize;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(RiftInitialize.MODID)
public class RiftSounds {
    @GameRegistry.ObjectHolder("prift.tyrannosaurus.idle")
    public static final SoundEvent TYRANNOSAURUS_IDLE = createSoundEvent("prift.tyrannosaurus.idle");

    @GameRegistry.ObjectHolder("prift.tyrannosaurus.hurt")
    public static final SoundEvent TYRANNOSAURUS_HURT = createSoundEvent("prift.tyrannosaurus.hurt");

    @GameRegistry.ObjectHolder("prift.tyrannosaurus.death")
    public static final SoundEvent TYRANNOSAURUS_DEATH = createSoundEvent("prift.tyrannosaurus.death");

    @GameRegistry.ObjectHolder("prift.tyrannosaurus.roar")
    public static final SoundEvent TYRANNOSAURUS_ROAR = createSoundEvent("prift.tyrannosaurus.roar");

    @GameRegistry.ObjectHolder("prift.stegosaurus.idle")
    public static final SoundEvent STEGOSAURUS_IDLE = createSoundEvent("prift.stegosaurus.idle");

    @GameRegistry.ObjectHolder("prift.stegosaurus.hurt")
    public static final SoundEvent STEGOSAURUS_HURT = createSoundEvent("prift.stegosaurus.hurt");

    @GameRegistry.ObjectHolder("prift.stegosaurus.death")
    public static final SoundEvent STEGOSAURUS_DEATH = createSoundEvent("prift.stegosaurus.death");

    @GameRegistry.ObjectHolder("prift.dodo.idle")
    public static final SoundEvent DODO_IDLE = createSoundEvent("prift.dodo.idle");

    @GameRegistry.ObjectHolder("prift.dodo.hurt")
    public static final SoundEvent DODO_HURT = createSoundEvent("prift.dodo.hurt");

    @GameRegistry.ObjectHolder("prift.dodo.death")
    public static final SoundEvent DODO_DEATH = createSoundEvent("prift.dodo.death");

    @GameRegistry.ObjectHolder("prift.triceratops.idle")
    public static final SoundEvent TRICERATOPS_IDLE = createSoundEvent("prift.triceratops.idle");

    @GameRegistry.ObjectHolder("prift.triceratops.hurt")
    public static final SoundEvent TRICERATOPS_HURT = createSoundEvent("prift.triceratops.hurt");

    @GameRegistry.ObjectHolder("prift.triceratops.death")
    public static final SoundEvent TRICERATOPS_DEATH = createSoundEvent("prift.triceratops.death");

    private static SoundEvent createSoundEvent(String soundName) {
        ResourceLocation soundID = new ResourceLocation(RiftInitialize.MODID, soundName);
        return new SoundEvent(soundID).setRegistryName(soundID);
    }
}
