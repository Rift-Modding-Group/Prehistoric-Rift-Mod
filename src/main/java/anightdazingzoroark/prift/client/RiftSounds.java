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

    @GameRegistry.ObjectHolder("prift.utahraptor.idle")
    public static final SoundEvent UTAHRAPTOR_IDLE = createSoundEvent("prift.utahraptor.idle");

    @GameRegistry.ObjectHolder("prift.utahraptor.hurt")
    public static final SoundEvent UTAHRAPTOR_HURT = createSoundEvent("prift.utahraptor.hurt");

    @GameRegistry.ObjectHolder("prift.utahraptor.death")
    public static final SoundEvent UTAHRAPTOR_DEATH = createSoundEvent("prift.utahraptor.death");

    @GameRegistry.ObjectHolder("prift.utahraptor.call")
    public static final SoundEvent UTAHRAPTOR_CALL = createSoundEvent("prift.utahraptor.call");

    @GameRegistry.ObjectHolder("prift.apatosaurus.idle")
    public static final SoundEvent APATOSAURUS_IDLE = createSoundEvent("prift.apatosaurus.idle");

    @GameRegistry.ObjectHolder("prift.apatosaurus.hurt")
    public static final SoundEvent APATOSAURUS_HURT = createSoundEvent("prift.apatosaurus.hurt");

    @GameRegistry.ObjectHolder("prift.apatosaurus.death")
    public static final SoundEvent APATOSAURUS_DEATH = createSoundEvent("prift.apatosaurus.death");

    @GameRegistry.ObjectHolder("prift.parasaurolophus.idle")
    public static final SoundEvent PARASAUROLOPHUS_IDLE = createSoundEvent("prift.parasaurolophus.idle");

    @GameRegistry.ObjectHolder("prift.parasaurolophus.hurt")
    public static final SoundEvent PARASAUROLOPHUS_HURT = createSoundEvent("prift.parasaurolophus.hurt");

    @GameRegistry.ObjectHolder("prift.parasaurolophus.death")
    public static final SoundEvent PARASAUROLOPHUS_DEATH = createSoundEvent("prift.parasaurolophus.death");

    @GameRegistry.ObjectHolder("prift.parasaurolophus.blow")
    public static final SoundEvent PARASAUROLOPHUS_BLOW = createSoundEvent("prift.parasaurolophus.blow");

    @GameRegistry.ObjectHolder("prift.dimetrodon.idle")
    public static final SoundEvent DIMETRODON_IDLE = createSoundEvent("prift.dimetrodon.idle");

    @GameRegistry.ObjectHolder("prift.dimetrodon.hurt")
    public static final SoundEvent DIMETRODON_HURT = createSoundEvent("prift.dimetrodon.hurt");

    @GameRegistry.ObjectHolder("prift.dimetrodon.death")
    public static final SoundEvent DIMETRODON_DEATH = createSoundEvent("prift.dimetrodon.death");

    @GameRegistry.ObjectHolder("prift.sarcosuchus.idle")
    public static final SoundEvent SARCOSUCHUS_IDLE = createSoundEvent("prift.sarcosuchus.idle");

    @GameRegistry.ObjectHolder("prift.sarcosuchus.hurt")
    public static final SoundEvent SARCOSUCHUS_HURT = createSoundEvent("prift.sarcosuchus.hurt");

    @GameRegistry.ObjectHolder("prift.sarcosuchus.death")
    public static final SoundEvent SARCOSUCHUS_DEATH = createSoundEvent("prift.sarcosuchus.death");

    private static SoundEvent createSoundEvent(String soundName) {
        ResourceLocation soundID = new ResourceLocation(RiftInitialize.MODID, soundName);
        return new SoundEvent(soundID).setRegistryName(soundID);
    }
}
