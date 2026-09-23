package anightdazingzoroark.prift.api.creature;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

/**
 * elemental damage is too obvious to explain
 * they also have special effects attached to them
 * */
public enum Element {
    FIRE((target, strength) -> {
        target.setFire((strength + 1) * 5 * 20);
    }),
    WATER((target, strength) -> {}),
    ELECTRIC((target, strength) -> {}),
    ICE((target, strength) -> {}),
    //like dragon element from MH
    MESOZOIC((target, strength) -> {}),
    SONIC((target, strength) -> {}),
    WIND((target, strength) -> {}),
    POISON((target, strength) -> {}),
    //"evil" or "death" or anything dark, like a dark or ghost type pokemon
    SHADOW((target, strength) -> {}),
    //bright lights basically
    LIGHT((target, strength) -> {});

    @NotNull
    public final BiConsumer<Entity, Integer> applyElementEffect;

    Element(@NotNull BiConsumer<Entity, Integer> applyElementEffect) {
        this.applyElementEffect = applyElementEffect;
    }
}
