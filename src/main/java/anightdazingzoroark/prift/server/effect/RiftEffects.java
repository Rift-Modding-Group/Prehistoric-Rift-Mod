package anightdazingzoroark.prift.server.effect;

import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class RiftEffects {
    public static Potion PARALYSIS;

    public static void registerEffects() {
        PARALYSIS = new RiftParalysisEffect();
    }

    @SubscribeEvent
    public void onEffectRegistry(@NotNull RegistryEvent.Register<Potion> event) {
        event.getRegistry().register(PARALYSIS);
    }
}
