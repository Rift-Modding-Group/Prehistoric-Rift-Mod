package anightdazingzoroark.prift.server.effect;

import net.minecraft.block.Block;
import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.List;

public class RiftEffects {
    public static final List<Potion> EFFECTS = new ArrayList<>();

    public static Potion PARALYSIS;

    public static void registerEffects() {
        PARALYSIS = registerEffect(new RiftEffectParalysis());
    }

    public static Potion registerEffect(Potion effect) {
        EFFECTS.add(effect);
        return effect;
    }

    @SubscribeEvent
    public void onEffectRegistry(RegistryEvent.Register<Potion> e) {
        IForgeRegistry<Potion> reg = e.getRegistry();
        reg.registerAll(EFFECTS.toArray(new Potion[0]));
    }
}
