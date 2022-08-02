package com.anightdazingzoroark.rift.registry;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModSounds {
    //tyrannosaurus
    public static final Identifier AMBIENT_TYRANNOSAURUS = new Identifier("rift:ambient_tyrannosaurus");
    public static SoundEvent AMBIENT_TYRANNOSAURUS_EVENT = new SoundEvent(AMBIENT_TYRANNOSAURUS);

    public static final Identifier HURT_TYRANNOSAURUS = new Identifier("rift:hurt_tyrannosaurus");
    public static SoundEvent HURT_TYRANNOSAURUS_EVENT = new SoundEvent(HURT_TYRANNOSAURUS);

    public static final Identifier DEATH_TYRANNOSAURUS = new Identifier("rift:death_tyrannosaurus");
    public static SoundEvent DEATH_TYRANNOSAURUS_EVENT = new SoundEvent(DEATH_TYRANNOSAURUS);

    public static final Identifier ROAR_TYRANNOSAURUS = new Identifier("rift:roar_tyrannosaurus");
    public static SoundEvent ROAR_TYRANNOSAURUS_EVENT = new SoundEvent(ROAR_TYRANNOSAURUS);

    public static void registerSounds() {
        //tyrannosaurus
        Registry.register(Registry.SOUND_EVENT, ModSounds.AMBIENT_TYRANNOSAURUS, AMBIENT_TYRANNOSAURUS_EVENT);
        Registry.register(Registry.SOUND_EVENT, ModSounds.HURT_TYRANNOSAURUS, HURT_TYRANNOSAURUS_EVENT);
        Registry.register(Registry.SOUND_EVENT, ModSounds.DEATH_TYRANNOSAURUS, DEATH_TYRANNOSAURUS_EVENT);
        Registry.register(Registry.SOUND_EVENT, ModSounds.ROAR_TYRANNOSAURUS, ROAR_TYRANNOSAURUS_EVENT);
    }
}
