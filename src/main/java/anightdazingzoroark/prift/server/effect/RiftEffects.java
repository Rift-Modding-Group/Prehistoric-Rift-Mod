package anightdazingzoroark.prift.server.effect;

import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class RiftEffects {
    public static Potion PARALYSIS;
    public static Potion DEFENSE_DOWN;

    public static void registerEffects() {
        PARALYSIS = new RiftParalysisEffect();
        DEFENSE_DOWN = new RiftDefenseDownEffect();
    }

    @SubscribeEvent
    public void onEffectRegistry(@NotNull RegistryEvent.Register<Potion> event) {
        event.getRegistry().register(PARALYSIS);
        event.getRegistry().register(DEFENSE_DOWN);
    }
}
