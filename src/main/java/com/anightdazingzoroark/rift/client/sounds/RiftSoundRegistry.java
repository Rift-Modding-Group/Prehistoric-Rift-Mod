package com.anightdazingzoroark.rift.client.sounds;

import com.anightdazingzoroark.rift.RiftInitialize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RiftSoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, RiftInitialize.MODID);

    public static final RegistryObject<SoundEvent> TYRANNOSAURUS_AMBIENT = registerSoundEvent("tyrannosaurus_ambient");
    public static final RegistryObject<SoundEvent> TYRANNOSAURUS_DEATH = registerSoundEvent("tyrannosaurus_death");
    public static final RegistryObject<SoundEvent> TYRANNOSAURUS_HURT = registerSoundEvent("tyrannosaurus_hurt");
    public static final RegistryObject<SoundEvent> TYRANNOSAURUS_ROAR = registerSoundEvent("tyrannosaurus_roar");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(RiftInitialize.MODID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
